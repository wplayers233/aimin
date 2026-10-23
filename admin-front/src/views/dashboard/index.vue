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
      <div v-for="k in kpis" :key="k.label" class="kpi" :class="`tone-${k.tone}`">
        <span class="label">{{ k.label }}</span>
        <div class="val">
          <span class="num" :data-count="k.value" :data-dec="k.dec || 0">0</span><small v-if="k.unit">{{ k.unit }}</small>
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
        <div class="chart" @mousemove="onChartMove" @mouseleave="hoverIdx = -1">
          <svg viewBox="0 0 660 230" preserveAspectRatio="none">
            <defs>
              <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#24415f" stop-opacity=".24" />
                <stop offset="100%" stop-color="#24415f" stop-opacity="0" />
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
            <line
              v-if="hoverIdx >= 0"
              class="crosshair"
              :x1="points[hoverIdx].x"
              :x2="points[hoverIdx].x"
              y1="16"
              y2="200"
            />
            <circle
              v-for="(p, i) in points"
              :key="i"
              class="pt"
              :class="{ hot: i === hoverIdx || i === hotIdx }"
              :cx="p.x"
              :cy="p.y"
              :r="i === hoverIdx ? 4.5 : 3"
            />
          </svg>
          <div v-if="hoverIdx >= 0" class="tip" :style="{ left: tipLeft, top: tipTop }">
            {{ points[hoverIdx].date }} · {{ points[hoverIdx].v }} 次
          </div>
        </div>

        <div class="heat-head">
          <h4>问答时段分布</h4>
          <span>7 DAYS × 24 HOURS</span>
        </div>
        <div class="heat">
          <template v-for="row in heatRows" :key="row.day">
            <span class="lab">{{ row.day }}</span>
            <i
              v-for="(lv, i) in row.cells"
              :key="i"
              :data-lv="lv || null"
              :title="row.day + ' ' + String(i).padStart(2, '0') + ':00'"
            ></i>
          </template>
        </div>
        <div class="heat-foot">
          <span>00:00 — 23:00</span>
          <span class="scale">少
            <i style="background: var(--paper-3)"></i>
            <i style="background: rgba(36, 65, 95, 0.26)"></i>
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
import heroImg from '@/assets/watercolor/processed/hero-bust.webp'

const userStore = useUserStore()
const docs = ref([])

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
  { label: '今日问答', value: 128, unit: '', delta: '+12.4%', tone: 'indigo', spark: 'M0 20 L14 17 L28 19 L42 12 L56 14 L70 8 L84 10 L100 4' },
  { label: '证据命中率', value: 96.7, dec: 1, unit: '%', delta: 'Recall@5', tone: 'sage', spark: 'M0 14 L14 12 L28 15 L42 11 L56 13 L70 9 L84 11 L100 7' },
  { label: '平均首字延迟', value: 1.8, dec: 1, unit: 's', delta: '-0.3s', tone: 'gold', spark: 'M0 8 L14 11 L28 9 L42 14 L56 12 L70 16 L84 15 L100 19' },
  { label: '风险拦截', value: 7, unit: '次', delta: '需确认', warn: true, tone: 'rose', spark: 'M0 19 L14 18 L28 20 L42 16 L56 17 L70 13 L84 15 L100 9' }
]

const points = [
  { x: 0, y: 168, v: 62, date: '10/10' },
  { x: 50, y: 152, v: 71, date: '10/11' },
  { x: 100, y: 160, v: 68, date: '10/12' },
  { x: 150, y: 128, v: 84, date: '10/13' },
  { x: 200, y: 140, v: 79, date: '10/14' },
  { x: 250, y: 104, v: 92, date: '10/15' },
  { x: 300, y: 118, v: 88, date: '10/16' },
  { x: 350, y: 74, v: 103, date: '10/17' },
  { x: 400, y: 88, v: 97, date: '10/18' },
  { x: 450, y: 58, v: 112, date: '10/19' },
  { x: 500, y: 66, v: 108, date: '10/20' },
  { x: 550, y: 36, v: 121, date: '10/21' },
  { x: 600, y: 44, v: 117, date: '10/22' },
  { x: 660, y: 20, v: 128, date: '10/23' }
]

const hoverIdx = ref(-1)
const hotIdx = 11
const tipLeft = computed(() => (hoverIdx.value >= 0 ? (points[hoverIdx.value].x / 660) * 100 + '%' : '0'))
const tipTop = computed(() => (hoverIdx.value >= 0 ? (points[hoverIdx.value].y / 230) * 100 - 12 + '%' : '0'))

function onChartMove(e) {
  const svg = e.currentTarget.querySelector('svg')
  if (!svg) return
  const rect = svg.getBoundingClientRect()
  const ratio = (e.clientX - rect.left) / rect.width
  const x = ratio * 660
  let best = 0
  let min = Infinity
  points.forEach((p, i) => {
    const d = Math.abs(p.x - x)
    if (d < min) {
      min = d
      best = i
    }
  })
  hoverIdx.value = best
}

function runCounters() {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  document.querySelectorAll('[data-count]').forEach((el) => {
    const target = parseFloat(el.dataset.count)
    const dec = parseInt(el.dataset.dec || '0', 10)
    if (reduce) {
      el.textContent = target.toFixed(dec)
      return
    }
    const t0 = performance.now()
    const dur = 1100
    const tick = (t) => {
      const p = Math.min(1, (t - t0) / dur)
      el.textContent = (target * (1 - Math.pow(1 - p, 3))).toFixed(dec)
      if (p < 1) requestAnimationFrame(tick)
    }
    requestAnimationFrame(tick)
  })
}

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

const heatRows = computed(() => {
  const days = ['一', '二', '三', '四', '五', '六', '日']
  let seed = 7
  const rnd = () => (seed = (seed * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff
  return days.map((day) => ({
    day,
    cells: Array.from({ length: 24 }, (_, h) => {
      const peak = (h >= 9 && h <= 11) || (h >= 15 && h <= 17)
      const base = peak ? 0.82 : h >= 8 && h <= 19 ? 0.42 : 0.12
      const v = base * (0.55 + rnd() * 0.6)
      return v > 0.72 ? 5 : v > 0.55 ? 4 : v > 0.38 ? 3 : v > 0.2 ? 2 : v > 0.08 ? 1 : 0
    })
  }))
})

onMounted(async () => {
  runCounters()
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
  height: 250px;
  overflow: hidden;
  mask-image: linear-gradient(to right, transparent 0%, #000 20%, #000 90%, transparent 100%),
    linear-gradient(to bottom, transparent 0%, #000 14%, #000 78%, transparent 100%);
  -webkit-mask-composite: source-in;
  mask-composite: intersect;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: 50% 6%;
    opacity: 1;
    filter: saturate(1.16) contrast(1.1);
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

    &[data-lv='1'] { background: rgba(36, 65, 95, 0.14); }
    &[data-lv='2'] { background: rgba(36, 65, 95, 0.26); }
    &[data-lv='3'] { background: rgba(36, 65, 95, 0.42); }
    &[data-lv='4'] { background: rgba(36, 65, 95, 0.6); }
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
    animation: bar-grow 1.1s var(--ease) 0.15s both;

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

/* ---- 交互与色彩层次 ---- */
.kpi {
  transition: background 0.3s ease;
  animation: kpi-in 0.6s var(--ease) both;

  &:nth-child(2) { animation-delay: 0.06s; }
  &:nth-child(3) { animation-delay: 0.12s; }
  &:nth-child(4) { animation-delay: 0.18s; }

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    height: 2px;
    width: 0;
    background: var(--tone, var(--indigo));
    transition: width 0.45s var(--ease);
  }

  &:hover {
    background: rgba(255, 255, 255, 0.22);
  }

  &:hover::before {
    width: 100%;
  }

  &:hover svg path {
    opacity: 1;
    stroke-width: 1.6;
  }

  svg path {
    stroke: var(--tone, var(--indigo));
  }
}

@keyframes bar-grow {
  from { width: 0; }
}

@keyframes kpi-in {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: none; }
}

.tone-indigo { --tone: #24415f; }
.tone-sage { --tone: #4f7358; }
.tone-gold { --tone: #a8792c; }
.tone-rose { --tone: #8a4b52; }

.chart {
  position: relative;
  cursor: crosshair;
}

.crosshair {
  stroke: rgba(19, 31, 43, 0.3);
  stroke-width: 1;
  stroke-dasharray: 3 3;
}

.tip {
  position: absolute;
  transform: translate(-50%, -100%);
  background: var(--ink);
  color: var(--paper);
  font-family: var(--mono);
  font-size: 10.5px;
  letter-spacing: 0.04em;
  padding: 5px 8px;
  border-radius: 2px;
  pointer-events: none;
  white-space: nowrap;
  box-shadow: 0 10px 24px -14px rgba(0, 0, 0, 0.6);
  z-index: 3;
}

.pt {
  transition: r 0.18s var(--ease), fill 0.18s;
}

.donut .val {
  animation: ring-in 1.4s var(--ease) 0.2s both;
}

@keyframes ring-in {
  from { stroke-dasharray: 0 999; }
  to { stroke-dasharray: 316 999; }
}

.donut:hover .val {
  stroke: #35587a;
}

.legend div {
  transition: transform 0.25s var(--ease);
}

.legend div:hover {
  transform: translateX(2px);
}

.bar-row {
  transition: background 0.25s;

  &:hover .bar-fill {
    opacity: 1;
  }
}

.lib-table tbody tr {
  transition: background 0.2s;
}


/* ---- 色彩层次收尾 ---- */
.kpi .val .num {
  color: var(--tone, var(--ink));
}

.panel-head h3::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 13px;
  background: var(--ochre);
  margin-right: 9px;
  vertical-align: -1px;
  opacity: 0.85;
}

.heat i[data-lv='1'] { background: rgba(79, 115, 88, 0.18); }
.heat i[data-lv='2'] { background: rgba(79, 115, 88, 0.34); }
.heat i[data-lv='3'] { background: rgba(36, 65, 95, 0.38); }
.heat i[data-lv='4'] { background: rgba(36, 65, 95, 0.6); }
.heat i[data-lv='5'] { background: var(--indigo); }

</style>
