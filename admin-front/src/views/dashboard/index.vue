<template>
  <div class="overview">
    <div class="hero">
      <div class="hero-copy">
        <span class="eyebrow">Clinical Overview · {{ today }}</span>
        <h2 class="serif-title">每一条结论，<br />都回到它的出处。</h2>
        <p class="lede">
          知识库共 {{ docCount }} 篇、{{ chunkCount }} 个切片；检索命中后由模型组织回答，
          角标对应原文段落，风险类问题在生成前拦截并二次确认。
        </p>
      </div>
      <div class="hero-art">
        <img :src="heroImg" alt="" />
      </div>
    </div>

    <div class="kpis">
      <div v-for="k in kpis" :key="k.label" class="kpi">
        <span class="label">{{ k.label }}</span>
        <div class="val">
          <span class="num">{{ k.value }}</span><small v-if="k.unit">{{ k.unit }}</small>
        </div>
        <span class="delta" :class="{ down: k.warn }">{{ k.delta }}</span>
        <svg viewBox="0 0 100 26" preserveAspectRatio="none">
          <path :d="k.spark" />
        </svg>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <div class="panel-head">
          <h3>近十四日咨询量</h3>
          <span class="meta">Q / DAY</span>
        </div>
        <div class="chart">
          <svg viewBox="0 0 660 230" preserveAspectRatio="none">
            <defs>
              <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#2a4a6b" stop-opacity=".16" />
                <stop offset="100%" stop-color="#2a4a6b" stop-opacity="0" />
              </linearGradient>
            </defs>
            <line class="grid-line" x1="0" y1="30" x2="660" y2="30" />
            <line class="grid-line" x1="0" y1="85" x2="660" y2="85" />
            <line class="grid-line" x1="0" y1="140" x2="660" y2="140" />
            <line class="grid-line" x1="0" y1="195" x2="660" y2="195" />
            <text class="axis-label" x="0" y="222">10/10</text>
            <text class="axis-label" x="300" y="222">10/16</text>
            <text class="axis-label" x="612" y="222">10/23</text>
            <path class="area" d="M0 168 L50 152 L100 160 L150 128 L200 140 L250 104 L300 118 L350 74 L400 88 L450 58 L500 66 L550 36 L600 44 L660 20 L660 200 L0 200 Z" />
            <path class="line" d="M0 168 L50 152 L100 160 L150 128 L200 140 L250 104 L300 118 L350 74 L400 88 L450 58 L500 66 L550 36 L600 44 L660 20" />
            <circle class="pt" cx="0" cy="168" r="3" />
            <circle class="pt" cx="150" cy="128" r="3" />
            <circle class="pt" cx="300" cy="118" r="3" />
            <circle class="pt" cx="450" cy="58" r="3" />
            <circle class="pt hot" cx="550" cy="36" r="4" />
            <circle class="pt" cx="660" cy="20" r="3" />
          </svg>
        </div>

        <div class="heat-head">
          <h4>问答时段分布</h4>
          <span>7 DAYS × 24 HOURS</span>
        </div>
        <div class="heat" ref="heatEl"></div>
        <div class="heat-foot">
          <span>00:00 — 23:00</span>
          <span class="scale">少
            <i style="background: var(--paper-3)"></i>
            <i style="background: rgba(42, 74, 107, 0.3)"></i>
            <i style="background: rgba(42, 74, 107, 0.5)"></i>
            <i style="background: var(--indigo)"></i>
            多</span>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h3>证据可回溯率</h3>
          <span class="meta">TRACEABLE</span>
        </div>
        <div class="donut-wrap">
          <div class="donut">
            <svg viewBox="0 0 120 120">
              <circle class="track" cx="60" cy="60" r="52" />
              <circle class="val" cx="60" cy="60" r="52" />
            </svg>
            <div class="center"><b>96.7</b><span>PERCENT</span></div>
          </div>
          <div class="legend">
            <div><i style="background: var(--indigo)"></i>带角标回答 122</div>
            <div><i style="background: #b9b2a2"></i>拒答（无可靠证据）8</div>
            <div><i style="background: var(--ochre)"></i>越界角标 0</div>
          </div>
        </div>

        <div class="rule"></div>

        <div class="panel-head tight">
          <h3 class="small">风险类别分布</h3>
          <span class="meta">30 D</span>
        </div>
        <div class="bars">
          <div v-for="b in riskBars" :key="b.name" class="bar-row">
            <span class="name">{{ b.name }}</span>
            <span class="bar-track">
              <i class="bar-fill" :class="{ warm: b.warm }" :style="{ width: b.w + '%' }"></i>
            </span>
            <span class="v">{{ b.v }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="trace">
      <div class="trace-head">
        <h3>一次提问的溯源路径</h3>
        <span class="meta">TRACE · 28 MS</span>
      </div>
      <svg viewBox="0 0 1120 200" preserveAspectRatio="xMinYMid meet">
        <rect class="pill" x="0" y="8" width="250" height="38" rx="3" />
        <text class="pill-t" x="16" y="32">向量召回 · 语义相近</text>
        <text class="pill-s" x="234" y="32" text-anchor="end">15</text>

        <rect class="pill" x="0" y="66" width="250" height="38" rx="3" />
        <text class="pill-t" x="16" y="90">BM25 · 字面精确</text>
        <text class="pill-s" x="234" y="90" text-anchor="end">15</text>

        <rect class="pill" x="0" y="124" width="250" height="38" rx="3" />
        <text class="pill-t" x="16" y="148">药品库 · 结构字段</text>
        <text class="pill-s" x="234" y="148" text-anchor="end">3</text>

        <path class="flow live" d="M250 27 C300 27 300 85 326 85" />
        <path class="flow live" d="M250 85 L326 85" />
        <path class="flow live" d="M250 143 C300 143 300 85 326 85" />

        <circle class="merge" cx="340" cy="85" r="14" />
        <text x="340" y="89" text-anchor="middle" class="merge-t">RRF</text>

        <path class="flow" d="M354 85 L382 85" marker-end="url(#arrowEnd)" />
        <rect class="pill" x="386" y="66" width="170" height="38" rx="3" />
        <text class="pill-t" x="402" y="90">门控 0.88 / 6.5</text>

        <path class="flow" d="M556 85 L584 85" marker-end="url(#arrowEnd)" />
        <rect class="pill" x="588" y="66" width="152" height="38" rx="3" />
        <text class="pill-t" x="604" y="90">证据 5 条</text>

        <path class="flow" d="M740 85 L768 85" marker-end="url(#arrowEnd)" />
        <rect class="pill" x="772" y="66" width="262" height="38" rx="3" />
        <text class="pill-t" x="788" y="90">回答 · 角标 [1]–[5]</text>

        <text x="0" y="188">每次提问都记录：命中来源、两路最高分、融合排名、最终角标范围</text>
        <text x="1120" y="188" text-anchor="end">三路 → 一份证据</text>
        <defs>
          <marker id="arrowEnd" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
            <path d="M2 1L8 5L2 9" fill="none" stroke="#16202e" stroke-opacity=".35" stroke-width="1.4" stroke-linecap="round" />
          </marker>
        </defs>
      </svg>
    </div>

    <div class="rule"></div>

    <div class="panel">
      <div class="panel-head">
        <h3>最近入库</h3>
        <span class="meta">{{ docCount }} DOCS · {{ chunkCount }} CHUNKS</span>
      </div>
      <table class="lib-table">
        <thead>
          <tr><th>文档</th><th>切片</th><th>大小</th><th>入库时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="d in recentDocs" :key="d.docId || d.fileName">
            <td>{{ d.fileName }}</td>
            <td class="num">{{ d.chunks }}</td>
            <td class="num">{{ formatSize(d.bytes) }}</td>
            <td class="num">{{ formatTime(d.ingestedAt) }}</td>
          </tr>
          <tr v-if="!recentDocs.length">
            <td colspan="4" class="empty">服务未连接，暂无文档数据</td>
          </tr>
        </tbody>
      </table>
      <p class="footnote">除知识库规模与命中率外，其余指标为演示数据。</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/store'
import { listDocs } from '@/api/agent'
import heroImg from '@/assets/watercolor/processed/hero.webp'

const userStore = useUserStore()
const docs = ref([])
const heatEl = ref(null)

const docCount = computed(() => docs.value.length)
const chunkCount = computed(() =>
  docs.value.reduce((sum, d) => sum + (Number(d?.chunks) || 0), 0)
)

const recentDocs = computed(() => docs.value.slice(0, 6))

const today = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const kpis = [
  { label: '今日问答', value: '128', unit: '', delta: '+12.4%', spark: 'M0 20 L14 17 L28 19 L42 12 L56 14 L70 8 L84 10 L100 4' },
  { label: '证据命中率', value: '96.7', unit: '%', delta: 'Recall@5', spark: 'M0 14 L14 12 L28 15 L42 11 L56 13 L70 9 L84 11 L100 7' },
  { label: '平均首字延迟', value: '1.8', unit: 's', delta: '-0.3s', spark: 'M0 8 L14 11 L28 9 L42 14 L56 12 L70 16 L84 15 L100 19' },
  { label: '风险拦截', value: '7', unit: '次', delta: '需确认', warn: true, spark: 'M0 19 L14 18 L28 20 L42 16 L56 17 L70 13 L84 15 L100 9' }
]

const riskBars = [
  { name: '剂量与用法', v: 34, w: 100 },
  { name: '联合用药', v: 21, w: 62 },
  { name: '特殊人群', v: 18, w: 53, warm: true },
  { name: '处方与操作', v: 12, w: 35 },
  { name: '剂量调整', v: 9, w: 26 }
]

function formatSize(bytes) {
  const n = Number(bytes) || 0
  if (!n) return '—'
  return n > 1024 * 1024 ? `${(n / 1024 / 1024).toFixed(1)} MB` : `${(n / 1024).toFixed(1)} KB`
}

function formatTime(ts) {
  const n = Number(ts)
  if (!n) return '—'
  const d = new Date(n)
  const p = (v) => String(v).padStart(2, '0')
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

function buildHeat() {
  const el = heatEl.value
  if (!el || el.childElementCount) return
  const days = ['一', '二', '三', '四', '五', '六', '日']
  let seed = 7
  const rnd = () => (seed = (seed * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff
  days.forEach((d) => {
    const lab = document.createElement('span')
    lab.className = 'lab'
    lab.textContent = d
    el.appendChild(lab)
    for (let h = 0; h < 24; h++) {
      const cell = document.createElement('i')
      const peak = (h >= 9 && h <= 11) || (h >= 15 && h <= 17)
      const base = peak ? 0.82 : h >= 8 && h <= 19 ? 0.42 : 0.12
      const v = base * (0.55 + rnd() * 0.6)
      const lv = v > 0.72 ? 5 : v > 0.55 ? 4 : v > 0.38 ? 3 : v > 0.2 ? 2 : v > 0.08 ? 1 : 0
      if (lv) cell.dataset.lv = lv
      cell.title = `周${d} ${String(h).padStart(2, '0')}:00 · ${Math.round(v * 40)} 次`
      el.appendChild(cell)
    }
  })
}

onMounted(async () => {
  buildHeat()
  try {
    const res = await listDocs()
    const list = res?.data ?? res
    if (Array.isArray(list)) docs.value = list
  } catch (e) {
    /* 后端未启动时保持空态 */
  }
})
</script>

<style scoped lang="scss">
.overview {
  display: flex;
  flex-direction: column;
}

.hero {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 34px;
  align-items: end;
  margin-bottom: 30px;
}

.hero-copy .lede {
  margin-top: 8px;
  color: var(--ink-2);
  font-size: 13.5px;
  max-width: 56ch;
}

.hero-art {
  position: relative;
  height: 220px;
  overflow: hidden;
  mask-image: linear-gradient(to right, transparent 0%, #000 22%, #000 88%, transparent 100%),
    linear-gradient(to bottom, transparent 0%, #000 18%, #000 100%);
  -webkit-mask-composite: source-in;
  mask-composite: intersect;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: 50% 42%;
    mix-blend-mode: multiply;
    opacity: 0.95;
    filter: saturate(0.95) contrast(0.98);
  }
}

.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
}

.kpi {
  position: relative;
  padding: 18px 20px 16px;
  border-right: 1px solid var(--line);

  &:last-child {
    border-right: none;
  }

  .label {
    display: block;
    font-family: var(--mono);
    font-size: 10.5px;
    letter-spacing: 0.14em;
    color: var(--ink-3);
    margin-bottom: 8px;
  }

  .val {
    font-family: var(--serif);
    font-size: 38px;
    line-height: 1;
    letter-spacing: -0.01em;

    small {
      font-size: 14px;
      font-family: var(--sans);
      color: var(--ink-2);
      margin-left: 4px;
    }
  }

  .delta {
    position: absolute;
    right: 18px;
    top: 20px;
    font-family: var(--mono);
    font-size: 10.5px;
    color: var(--sage);

    &.down {
      color: var(--ochre);
    }
  }

  svg {
    display: block;
    margin-top: 12px;
    width: 100%;
    height: 26px;
    overflow: visible;

    path {
      fill: none;
      stroke: var(--indigo);
      stroke-width: 1.2;
      stroke-linecap: round;
      opacity: 0.42;
      transition: opacity 0.3s, stroke-width 0.3s;
    }
  }

  &:hover svg path {
    opacity: 0.9;
    stroke-width: 1.5;
  }
}

.grid-2 {
  display: grid;
  grid-template-columns: 1.55fr 1fr;
  gap: 28px;
  margin-top: 28px;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 14px;

  h3 {
    font-family: var(--serif);
    font-size: 17px;
    font-weight: 400;

    &.small {
      font-size: 14px;
    }
  }

  &.tight {
    margin-bottom: 6px;
  }

  .meta {
    font-family: var(--mono);
    font-size: 10.5px;
    letter-spacing: 0.12em;
    color: var(--ink-3);
  }
}

.chart {
  height: 240px;

  svg {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .grid-line {
    stroke: var(--line-2);
    stroke-width: 1;
    stroke-dasharray: 2 4;
  }

  .axis-label {
    font-family: var(--mono);
    font-size: 9.5px;
    fill: var(--ink-3);
    letter-spacing: 0.06em;
  }

  .area {
    fill: url(#areaGrad);
  }

  .line {
    fill: none;
    stroke: var(--indigo);
    stroke-width: 1.6;
    stroke-linejoin: round;
    stroke-linecap: round;
    stroke-dasharray: 2000;
    stroke-dashoffset: 2000;
    animation: draw 2.2s var(--ease) 0.3s forwards;
  }

  .pt {
    fill: var(--paper);
    stroke: var(--indigo);
    stroke-width: 1.4;

    &.hot {
      fill: var(--ochre);
      stroke: var(--ochre);
    }
  }

  @keyframes draw {
    to {
      stroke-dashoffset: 0;
    }
  }
}

.heat-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 18px 0 8px;

  h4 {
    font-family: var(--serif);
    font-size: 14px;
    font-weight: 400;
  }

  span {
    font-family: var(--mono);
    font-size: 10px;
    letter-spacing: 0.12em;
    color: var(--ink-3);
  }
}

.heat {
  display: grid;
  grid-template-columns: 20px repeat(24, 1fr);
  gap: 3px;
  max-width: 640px;

  .lab {
    font-family: var(--mono);
    font-size: 9px;
    color: var(--ink-3);
    line-height: 12px;
    height: 12px;
  }

  i {
    display: block;
    aspect-ratio: 1;
    border-radius: 2px;
    background: var(--paper-3);
    transition: transform 0.18s var(--ease), background 0.18s;

    &[data-lv='1'] { background: rgba(42, 74, 107, 0.16); }
    &[data-lv='2'] { background: rgba(42, 74, 107, 0.3); }
    &[data-lv='3'] { background: rgba(42, 74, 107, 0.48); }
    &[data-lv='4'] { background: rgba(42, 74, 107, 0.68); }
    &[data-lv='5'] { background: var(--indigo); }

    &:hover {
      transform: scale(1.45);
      background: var(--ochre);
      position: relative;
      z-index: 2;
    }
  }
}

.heat-foot {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-family: var(--mono);
  font-size: 9.5px;
  color: var(--ink-3);
  letter-spacing: 0.08em;
  max-width: 640px;

  .scale {
    display: flex;
    align-items: center;
    gap: 4px;

    i {
      width: 9px;
      height: 9px;
      display: block;
      border-radius: 1.5px;
    }
  }
}

.donut-wrap {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-top: 4px;
}

.donut {
  position: relative;
  width: 128px;
  height: 128px;
  flex: 0 0 128px;

  svg {
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
  }

  circle {
    fill: none;
    stroke-width: 6;
  }

  .track {
    stroke: var(--paper-3);
  }

  .val {
    stroke: var(--indigo);
    stroke-linecap: round;
    stroke-dasharray: 316 999;
  }

  .center {
    position: absolute;
    inset: 0;
    display: grid;
    place-items: center;
    text-align: center;

    b {
      display: block;
      font-family: var(--serif);
      font-size: 26px;
      font-weight: 400;
      line-height: 1;
    }

    span {
      font-family: var(--mono);
      font-size: 9px;
      color: var(--ink-3);
      letter-spacing: 0.12em;
    }
  }
}

.legend {
  display: flex;
  flex-direction: column;
  gap: 11px;
  font-size: 12px;

  div {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--ink-2);
  }

  i {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    display: inline-block;
  }
}

.bars {
  display: flex;
  flex-direction: column;
  gap: 13px;
  margin-top: 4px;
}

.bar-row {
  display: grid;
  grid-template-columns: 76px 1fr 32px;
  align-items: center;
  gap: 12px;
  font-size: 12px;

  .name {
    color: var(--ink-2);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .bar-track {
    height: 6px;
    background: var(--paper-3);
    border-radius: 3px;
    overflow: hidden;
  }

  .bar-fill {
    display: block;
    height: 100%;
    background: var(--indigo);
    opacity: 0.85;
    border-radius: 3px;
    transition: width 1.2s var(--ease);

    &.warm {
      background: var(--ochre);
      opacity: 0.8;
    }
  }

  .v {
    font-family: var(--mono);
    font-size: 11px;
    color: var(--ink-3);
    text-align: right;
  }
}

.trace {
  margin-top: 28px;
  border-top: 1px solid var(--line);
  padding-top: 20px;

  svg {
    width: 100%;
    height: 190px;
    display: block;
    overflow: visible;
  }

  .flow {
    fill: none;
    stroke: var(--line);
    stroke-width: 1;
    stroke-dasharray: 3 4;

    &.live {
      stroke: var(--indigo);
      stroke-opacity: 0.4;
      stroke-dasharray: none;
      animation: dashmove 6s linear infinite;
    }
  }

  @keyframes dashmove {
    to {
      stroke-dashoffset: -60;
    }
  }

  text {
    font-family: var(--mono);
    font-size: 10.5px;
    fill: var(--ink-3);
    letter-spacing: 0.04em;
  }

  .pill {
    fill: rgba(42, 74, 107, 0.06);
    stroke: none;
  }

  .pill-t {
    font-family: var(--sans);
    font-size: 12.5px;
    fill: var(--ink);
    letter-spacing: 0;
  }

  .pill-s {
    font-family: var(--mono);
    font-size: 10.5px;
    fill: var(--ink-3);
  }

  .merge {
    fill: var(--indigo);
  }

  .merge-t {
    fill: var(--paper);
    font-size: 9px;
  }
}

.trace-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 6px;

  h3 {
    font-family: var(--serif);
    font-size: 17px;
    font-weight: 400;
  }

  .meta {
    font-family: var(--mono);
    font-size: 10.5px;
    letter-spacing: 0.12em;
    color: var(--ink-3);
  }
}

.lib-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12.5px;

  th {
    text-align: left;
    font-family: var(--mono);
    font-weight: 400;
    font-size: 10px;
    letter-spacing: 0.14em;
    color: var(--ink-3);
    padding: 0 12px 8px 0;
    border-bottom: 1px solid var(--line);
  }

  td {
    padding: 11px 12px 11px 0;
    border-bottom: 1px solid var(--line-2);
    color: var(--ink-2);

    &:first-child {
      color: var(--ink);
    }
  }

  .empty {
    color: var(--ink-3);
    text-align: center;
    padding: 24px 0;
  }
}

.footnote {
  margin-top: 12px;
  font-family: var(--mono);
  font-size: 10px;
  color: var(--ink-3);
  letter-spacing: 0.06em;
}

@media (max-width: 1100px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }
  .hero {
    grid-template-columns: 1fr;
  }
  .hero-art {
    height: 170px;
  }
  .kpis {
    grid-template-columns: 1fr 1fr;
  }
  .kpi:nth-child(2) {
    border-right: none;
  }
}
</style>
