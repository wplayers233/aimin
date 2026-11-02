import json, math, re, sys, collections

TOKEN = re.compile(r'[\u4e00-\u9fa5]+|[a-z0-9][a-z0-9._-]*')

def tokenize(text):
    out = []
    if not text or not text.strip():
        return out
    for m in TOKEN.finditer(text.lower()):
        tok = m.group()
        if '\u4e00' <= tok[0] <= '\u9fa5':
            if len(tok) == 1:
                out.append(tok)
            else:
                for i in range(len(tok) - 1):
                    out.append(tok[i:i+2])
        elif len(tok) >= 2 or tok.isdigit():
            out.append(tok)
    return out

class Drug:
    __slots__ = ('name','generic','form','strength','manu','desc','tokens')

def load_drugs(path):
    rows = []
    with open(path, encoding='utf-8') as f:
        for line in f:
            line = line.rstrip('\n')
            if not line:
                continue
            parts = line.split('\t')
            if len(parts) < 7:
                continue
            d = Drug()
            d.name, d.generic, d.form, d.strength, d.manu, d.desc = (p.strip() for p in parts[1:7])
            text = ' '.join(x for x in (d.name, d.generic, d.form, d.strength, d.desc) if x) + ' '
            d.tokens = collections.Counter(tokenize(text))
            rows.append(d)
    return rows

def drug_retrieve(drugs, docfreq, total, query, topk):
    qt = tokenize(query)
    if not qt:
        return []
    unique = list(dict.fromkeys(qt))
    nq = query.lower()
    scored = []
    for d in drugs:
        s = 0.0
        for t in unique:
            freq = d.tokens.get(t, 0)
            if not freq:
                continue
            df = docfreq.get(t, 0)
            idf = math.log(1 + (total - df + 0.5) / (df + 0.5))
            s += idf * (1 + math.log(freq))
        if len(d.name) >= 2 and d.name.lower() in nq:
            s += 8
        if len(d.generic) >= 2 and d.generic.lower() in nq:
            s += 6
        if len(nq) >= 2 and query in d.name:
            s += 4
        if s > 0:
            scored.append((d, s))
    scored.sort(key=lambda x: -x[1])
    return scored[:topk]

def rrf_merge(kb_n, drug_n, topk=5):
    """精确复刻 RrfFusion：只按名次，kb 先插入，稳定排序"""
    K = 60
    items = []
    for i in range(kb_n):
        items.append(('kb', 1.0 / (K + i + 1)))
    for j in range(drug_n):
        items.append(('drug', 1.0 / (K + j + 1)))
    items.sort(key=lambda x: -x[1])
    top = items[:topk]
    return sum(1 for t, _ in top if t == 'kb'), sum(1 for t, _ in top if t == 'drug')

def main():
    drugs = load_drugs(r't_drug_dump.tsv')
    total = len(drugs)
    docfreq = collections.Counter()
    for d in drugs:
        for t in d.tokens:
            docfreq[t] += 1

    qs = json.load(open('../../server/data/eval/questions.json', encoding='utf-8'))['questions']
    latest = json.load(open('../../server/data/eval/latest.json', encoding='utf-8'))
    # fact/unanswerable 的 diagnostics 会写 hybridTop（空则缺省=0）；drug/risk 不写 -> None（未知）
    kbn = {}
    for d in latest['diagnostics']:
        t = next((q['type'] for q in qs if q['id'] == d['id']), '')
        if 'hybridTop' in d:
            kbn[d['id']] = len(d['hybridTop'])
        else:
            kbn[d['id']] = 0 if t in ('fact', 'unanswerable') else None

    print(f'药品库 {total} 条 | 题 {len(qs)} 道\n')
    print(f"{'id':<5}{'type':<14}{'KB':<4}{'药品':<5}{'合并后':<10}药品命中")
    print('-' * 88)
    stats = collections.Counter()
    drug_hits_on_unanswerable = []
    displaced = []
    for q in qs:
        hits = drug_retrieve(drugs, docfreq, total, q['question'], 3)
        dn = len(hits)
        kb = kbn.get(q['id'])
        stats[q['type']] += 1
        if q['type'] == 'unanswerable' and dn:
            drug_hits_on_unanswerable.append((q['id'], q['question'], [(d.name, round(s, 2)) for d, s in hits]))
        comp = '未记录' if kb is None else ''
        if kb is not None:
            kbs, ds = rrf_merge(kb, dn)
            comp = f'{kbs}KB+{ds}药'
            if kb >= 4 and dn >= 2:
                displaced.append((q['id'], kb, dn, kbs))
        print(f"{q['id']:<5}{q['type']:<14}{('-' if kb is None else kb):<4}{dn:<5}{comp:<10}"
              + ', '.join(d.name for d, _ in hits))

    print('\n=== 汇总 ===')
    print('题量分布:', dict(stats))
    print(f'\n不可答题中"药品库有命中"的题数: {len(drug_hits_on_unanswerable)} / 8')
    for qid, q, hits in drug_hits_on_unanswerable:
        print(f'  {qid} {q} -> {hits}')
    print(f'\nKB 原本够 4 条以上、且药品 ≥2 条（第 4/5 条证据被挤掉）的题数: {len(displaced)}')
    for row in displaced:
        print(f'  {row[0]}  KB={row[1]} 药品={row[2]} -> 合并后 KB={row[3]}')

    # 全量：KB 名额损失分布
    loss = collections.Counter()
    for q in qs:
        hits = drug_retrieve(drugs, docfreq, total, q['question'], 3)
        kb = kbn.get(q['id'])
        if kb is None or kb == 0:
            continue
        kbs, _ = rrf_merge(kb, len(hits))
        loss[kb - kbs] += 1
    print('\nKB 名额被挤掉的条数分布（仅统计 KB 命中 ≥1 的题：30 道 fact）:', dict(sorted(loss.items())))

if __name__ == '__main__':
    main()
