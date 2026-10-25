<template>
  <div class="overview">
    <!-- 问候区：大标题 + 水彩解剖图，数据全部来自真实接口 -->
    <section class="hero">
      <div class="hero-copy">
        <span class="eyebrow">CLINICAL WORKBENCH · {{ today }} 星期{{ weekday }}</span>
        <h1 class="headline">{{ greeting }}，{{ displayName }}。</h1>
        <p class="lede">
          知识库收录 <b>{{ docsCount }}</b> 篇临床文档，每条回答都附原文出处；
          涉及处方与剂量的提问，会先请您二次确认。
        </p>
        <div class="hero-actions">
          <button class="cta" @click="goWorkbench">
            进入诊断工作台
            <i class="arrow">→</i>
          </button>
          <span
            v-if="isDemo"
            class="demo-pill"
            title="未配置生成模型密钥，回答为固定模板"
          >演示模式 · 未接入生成模型</span>
        </div>
      </div>
      <div class="hero-art">
        <img :src="heroImg" alt="" />
      </div>
    </section>

    <!-- 运行数据：四张浮卡，数字全部来自落库统计 -->
    <section class="block">
      <div class="block-head">
        <h2>运行数据</h2>
        <span class="meta">来自本地运行记录 · 保留 90 天</span>
      </div>
      <div class="metrics">
        <div v-for="m in metricCards" :key="m.label" class="metric" :style="{ '--tone': m.tone }">
          <span class="m-label">{{ m.label }}</span>
          <div class="m-val">
            <template v-if="statsLoaded">
              <span class="num" :data-count="m.value">0</span><small>{{ m.unit }}</small>
            </template>
            <span v-else class="pending">—</span>
          </div>
          <span class="m-sub">{{ m.sub }}</span>
        </div>
      </div>
    </section>

    <!-- 近 14 日咨询量 + 最近会话 -->
    <section class="grid-2">
      <div class="card">
        <div class="panel-head">
          <h3>近十四日咨询量</h3>
          <span class="meta">QUESTIONS / DAY</span>
        </div>
        <div v-if="statsLoaded && hasDaily" class="chart" @mousemove="onChartMove" @mouseleave="hoverIdx = -1">
          <svg viewBox="0 0 660 250" preserveAspectRatio="none">
            <defs>
              <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#4a786d" stop-opacity=".22" />
                <stop offset="100%" stop-color="#4a786d" stop-opacity="0" />
              </linearGradient>
            </defs>
            <line class="grid-line" x1="0" y1="30" x2="660" y2="30" />
            <line class="grid-line" x1="0" y1="80" x2="660" y2="80" />
            <line class="grid-line" x1="0" y1="130" x2="660" y2="130" />
            <line class="grid-line" x1="0" y1="180" x2="660" y2="180" />
            <text class="axis-label" x="0" y="240">{{ axisLabels[0] }}</text>
            <text class="axis-label" x="300" y="240">{{ axisLabels[1] }}</text>
            <text class="axis-label" x="618" y="240">{{ axisLabels[2] }}</text>
            <path class="area" :d="areaPath" />
            <path class="line" :d="linePath" />
            <line
              v-if="hoverIdx >= 0"
              class="crosshair"
              :x1="chartPoints[hoverIdx].x"
              :x2="chartPoints[hoverIdx].x"
              y1="18"
              y2="205"
            />
            <circle
              v-for="(p, i) in chartPoints"
              :key="i"
              class="pt"
              :class="{ hot: i === hoverIdx || i === chartPoints.length - 1 }"
              :cx="p.x"
              :cy="p.y"
              :r="i === hoverIdx ? 4.5 : 3"
            />
          </svg>
          <div v-if="hoverIdx >= 0" class="tip" :style="{ left: tipLeft, top: tipTop }">
            {{ chartPoints[hoverIdx].date }} · {{ chartPoints[hoverIdx].v }} 次
          </div>
        </div>
        <div v-else class="empty">{{ statsLoaded ? '暂无咨询记录' : '正在加载…' }}</div>
      </div>

      <div class="card">
        <div class="panel-head">
          <h3>最近会话</h3>
          <span class="meta">SESSIONS</span>
        </div>
        <div v-if="sessionsLoaded && recentSessions.length" class="s-list">
          <div v-for="s in recentSessions" :key="s.sessionId" class="s-row">
            <span class="s-time">{{ formatTime(s.updatedAt) }}</span>
            <span class="s-title" :title="s.title || '未命名会话'">{{ s.title || '未命名会话' }}</span>
            <span class="s-count">{{ s.messageCount }} 条</span>
          </div>
        </div>
        <div v-else class="empty">{{ sessionsLoaded ? '暂无会话记录' : '正在加载…' }}</div>
      </div>
    </section>

    <!-- 最近入库 -->
    <section class="card last">
      <div class="panel-head">
        <h3>最近入库</h3>
        <span class="meta">{{ docsCount }} DOCS</span>
      </div>
      <table class="lib-table">
        <thead>
          <tr><th>文档</th><th>大小</th><th>入库时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="d in recentDocs" :key="d.docId || d.fileName">
            <td>{{ d.fileName }}</td>
            <td class="num">{{ formatSize(d.bytes) }}</td>
            <td class="num">{{ formatTime(d.ingestedAt) }}</td>
          </tr>
          <tr v-if="docsLoaded && !recentDocs.length">
            <td colspan="3" class="empty">暂无文档数据</td>
          </tr>
        </tbody>
      </table>
    </section>

    <footer class="foot">
      <span>本系统仅供技术研究与演示，不构成诊疗建议</span>
      <span>AIMIN · CLINICAL ATLAS</span>
    </footer>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useAppStore } from '@/store'
import { getStats, listDocs, listSessions } from '@/api/agent'
import heroImg from '@/assets/watercolor/processed/hero-bust.webp'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// ---------- 真实数据 ----------
const stats = ref(null)
const statsLoaded = ref(false)
const sessions = ref([])
const sessionsLoaded = ref(false)
const docs = ref([])
const docsLoaded = ref(false)

const isDemo = computed(() => appStore.isDemoMode)
const docsCount = computed(() => docs.value.length || Number(appStore.agentStatus?.docs) || 0)

const metricCards = computed(() => [
  { label: '累计会话', value: stats.value?.sessions ?? 0, unit: '个', sub: '独立咨询会话', tone: 'var(--indigo)' },
  { label: '累计提问', value: stats.value?.questions ?? 0, unit: '次', sub: '含被拦截的提问', tone: 'var(--sage-deep)' },
  {
    label: '风险拦截',
    value: stats.value?.riskInterrupted ?? 0,
    unit: '次',
    sub: `已确认 ${stats.value?.riskConfirmed ?? 0} · 放弃 ${stats.value?.riskRejected ?? 0}`,
    tone: 'var(--ochre-deep)'
  },
  { label: '知识库文档', value: docsCount.value, unit: '篇', sub: '回答均附原文出处', tone: 'var(--gold)' }
])

// ---------- 问候与日期 ----------
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 5) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
const displayName = computed(() => {
  const n = (userStore.nickname || '').trim()
  return n || '你好'
})
const today = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})
const weekday = computed(() => ['日', '一', '二', '三', '四', '五', '六'][new Date().getDay()])

function goWorkbench() {
  router.push('/agent/index')
}

// ---------- 折线图：由真实 daily 数据生成坐标 ----------
const W = 660
const TOP = 22
const BOTTOM = 205

const chartPoints = computed(() => {
  const daily = stats.value?.daily ?? []
  if (!daily.length) return []
  const qs = daily.map((d) => Number(d.questions) || 0)
  const max = Math.max(1, ...qs)
  const span = BOTTOM - TOP
  const step = daily.length > 1 ? W / (daily.length - 1) : W
  return daily.map((d, i) => ({
    x: Number((i * step).toFixed(1)),
    y: Number((BOTTOM - (qs[i] / max) * span).toFixed(1)),
    v: qs[i],
    date: d.date
  }))
})

const hasDaily = computed(() => chartPoints.value.some((p) => p.v > 0))
const linePath = computed(() =>
  chartPoints.value.map((p, i) => `${i ? 'L' : 'M'}${p.x} ${p.y}`).join(' ')
)
const areaPath = computed(() =>
  chartPoints.value.length ? `${linePath.value} L${W} ${BOTTOM} L0 ${BOTTOM} Z` : ''
)
const axisLabels = computed(() => {
  const pts = chartPoints.value
  if (pts.length < 3) return ['', '', '']
  const mid = pts[Math.floor(pts.length / 2)]
  return [pts[0].date, mid.date, pts[pts.length - 1].date]
})

const hoverIdx = ref(-1)
const tipLeft = computed(() =>
  hoverIdx.value >= 0 ? (chartPoints.value[hoverIdx.value].x / W) * 100 + '%' : '0'
)
const tipTop = computed(() =>
  hoverIdx.value >= 0 ? (chartPoints.value[hoverIdx.value].y / 250) * 100 - 12 + '%' : '0'
)

function onChartMove(e) {
  const svg = e.currentTarget.querySelector('svg')
  if (!svg) return
  const rect = svg.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * W
  let best = 0
  let min = Infinity
  chartPoints.value.forEach((p, i) => {
    const d = Math.abs(p.x - x)
    if (d < min) {
      min = d
      best = i
    }
  })
  hoverIdx.value = best
}

// ---------- 数字滚动（数据到达后触发；帧被挂起时兜底显示真实数值） ----------
function runCounters() {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  document.querySelectorAll('.metric [data-count]').forEach((el) => {
    const target = parseFloat(el.dataset.count) || 0
    if (reduce) {
      el.textContent = String(target)
      return
    }
    const t0 = performance.now()
    const dur = 1000
    let done = false
    const tick = (t) => {
      const p = Math.min(1, (t - t0) / dur)
      el.textContent = String(Math.round(target * (1 - Math.pow(1 - p, 3))))
      if (p < 1) requestAnimationFrame(tick)
      else done = true
    }
    requestAnimationFrame(tick)
    setTimeout(() => {
      if (!done) el.textContent = String(target)
    }, dur + 250)
  })
}

// ---------- 列表 ----------
const recentSessions = computed(() => sessions.value.slice(0, 8))
const recentDocs = computed(() => docs.value.slice(0, 6))

function formatSize(bytes) {
  const n = Number(bytes) || 0
  if (!n) return '—'
  return n > 1024 * 1024 ? `${(n / 1024 / 1024).toFixed(1)} MB` : `${(n / 1024).toFixed(1)} KB`
}

function formatTime(ts) {
  if (!ts) return '—'
  const d = typeof ts === 'number' ? new Date(ts) : new Date(String(ts).replace(' ', 'T'))
  if (Number.isNaN(d.getTime())) return String(ts)
  const p = (v) => String(v).padStart(2, '0')
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

onMounted(async () => {
  // 四路数据互不阻塞：单接口失败只影响对应面板的空态
  const [st, ss, dd] = await Promise.allSettled([
    getStats(14),
    listSessions(8),
    listDocs()
  ])
  if (st.status === 'fulfilled') stats.value = st.value?.data ?? null
  statsLoaded.value = true
  if (ss.status === 'fulfilled') {
    const list = ss.value?.data ?? ss.value
    if (Array.isArray(list)) sessions.value = list
  }
  sessionsLoaded.value = true
  if (dd.status === 'fulfilled') {
    const list = dd.value?.data ?? dd.value
    if (Array.isArray(list)) docs.value = list
  }
  docsLoaded.value = true
  // 状态复用 layout 已发起的请求；demo 徽标与文档数兜底
  await appStore.loadAgentStatus()
  await nextTick()
  runCounters()
})
</script>

<style scoped lang="scss">
.overview {
  display: flex;
  flex-direction: column;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.hero {
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 40px;
  align-items: end;
  animation: rise 0.8s var(--ease) both;
}

.eyebrow {
  display: block;
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.22em;
  color: var(--ink-3);
}

.headline {
  margin: 14px 0 0;
  font-family: var(--serif);
  font-weight: 400;
  font-size: clamp(36px, 4.2vw, 54px);
  line-height: 1.16;
  letter-spacing: -0.01em;
  color: var(--ink);
}

.lede {
  margin: 18px 0 0;
  font-size: 15px;
  line-height: 1.9;
  color: var(--ink-2);
  max-width: 44ch;

  b {
    color: var(--indigo);
    font-weight: 600;
  }
}

.hero-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 30px;
}

.cta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border: none;
  cursor: pointer;
  background: var(--indigo);
  color: #f4f7f3;
  font-family: var(--sans);
  font-size: 14px;
  letter-spacing: 0.06em;
  padding: 13px 30px;
  border-radius: 999px;
  box-shadow: 0 14px 28px -14px rgba(67, 111, 100, 0.55);
  transition: background 0.3s var(--ease), transform 0.3s var(--ease), box-shadow 0.3s var(--ease);

  .arrow {
    font-style: normal;
    transition: transform 0.3s var(--ease);
  }

  &:hover {
    background: #3a6157;
    transform: translateY(-1px);
    box-shadow: 0 18px 34px -14px rgba(67, 111, 100, 0.62);

    .arrow {
      transform: translateX(3px);
    }
  }

  &:active {
    transform: translateY(0);
  }
}

.demo-pill {
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--ochre-deep);
  border: 1px solid rgba(176, 126, 92, 0.35);
  background: var(--ochre-soft);
  padding: 7px 15px;
  border-radius: 999px;
}

.hero-art {
  position: relative;
  height: 320px;
  overflow: hidden;
  mask-image: linear-gradient(to right, transparent 0%, #000 22%, #000 88%, transparent 100%),
    linear-gradient(to bottom, transparent 0%, #000 16%, #000 80%, transparent 100%);
  -webkit-mask-composite: source-in;
  mask-composite: intersect;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: 50% 8%;
    filter: saturate(1.14) contrast(1.08);
  }
}

.block {
  margin-top: 60px;
  animation: rise 0.8s var(--ease) 0.08s both;
}

.block-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-family: var(--serif);
    font-weight: 400;
    font-size: 24px;
    color: var(--ink);
  }

  .meta {
    font-family: var(--mono);
    font-size: 10.5px;
    letter-spacing: 0.12em;
    color: var(--ink-3);
  }
}

.metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.metric {
  position: relative;
  overflow: hidden;
  background: #f6f8f4;
  border: 1px solid var(--line-2);
  border-radius: 20px;
  padding: 26px 26px 22px;
  transition: transform 0.4s var(--ease), box-shadow 0.4s var(--ease);

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
    transform: translateY(-4px);
    box-shadow: 0 2px 4px rgba(44, 56, 52, 0.04), 0 20px 44px -20px rgba(44, 56, 52, 0.22);

    &::before {
      width: 100%;
    }
  }

  .m-label {
    display: block;
    font-family: var(--mono);
    font-size: 10.5px;
    letter-spacing: 0.16em;
    color: var(--ink-3);
  }

  .m-val {
    margin-top: 14px;
    font-family: var(--serif);
    font-size: 44px;
    line-height: 1;
    color: var(--tone, var(--ink));

    small {
      font-family: var(--sans);
      font-size: 13px;
      color: var(--ink-2);
      margin-left: 5px;
    }

    .pending {
      color: var(--ink-3);
    }
  }

  .m-sub {
    display: block;
    margin-top: 12px;
    font-size: 12px;
    color: var(--ink-3);
  }
}

.grid-2 {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 18px;
  margin-top: 18px;
  animation: rise 0.8s var(--ease) 0.16s both;
}

.card {
  background: #f6f8f4;
  border: 1px solid var(--line-2);
  border-radius: 20px;
  padding: 26px 28px;

  &.last {
    margin-top: 18px;
    animation: rise 0.8s var(--ease) 0.24s both;
  }
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18px;

  h3 {
    margin: 0;
    font-family: var(--serif);
    font-weight: 400;
    font-size: 17px;
    color: var(--ink);

    &::before {
      content: '';
      display: inline-block;
      width: 3px;
      height: 13px;
      background: var(--ochre);
      margin-right: 9px;
      vertical-align: -1px;
      opacity: 0.85;
    }
  }

  .meta {
    font-family: var(--mono);
    font-size: 10.5px;
    letter-spacing: 0.12em;
    color: var(--ink-3);
  }
}

.chart {
  position: relative;
  height: 260px;
  cursor: crosshair;

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
    stroke-width: 1.8;
    stroke-linejoin: round;
    stroke-linecap: round;
    stroke-dasharray: 2200;
    stroke-dashoffset: 2200;
    animation: draw 2s var(--ease) 0.3s forwards;
  }

  .pt {
    fill: #f6f8f4;
    stroke: var(--indigo);
    stroke-width: 1.4;
    transition: r 0.18s var(--ease), fill 0.18s;

    &.hot {
      fill: var(--ochre);
      stroke: var(--ochre);
    }
  }

  .crosshair {
    stroke: rgba(44, 56, 52, 0.3);
    stroke-width: 1;
    stroke-dasharray: 3 3;
  }

  @keyframes draw {
    to {
      stroke-dashoffset: 0;
    }
  }
}

.tip {
  position: absolute;
  transform: translate(-50%, -100%);
  background: var(--ink);
  color: var(--paper);
  font-family: var(--mono);
  font-size: 10.5px;
  letter-spacing: 0.04em;
  padding: 5px 9px;
  border-radius: 6px;
  pointer-events: none;
  white-space: nowrap;
  box-shadow: 0 10px 24px -14px rgba(0, 0, 0, 0.6);
  z-index: 3;
}

.s-list {
  display: flex;
  flex-direction: column;
}

.s-row {
  display: grid;
  grid-template-columns: 74px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid var(--line-2);
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.5);
  }

  .s-time {
    font-family: var(--mono);
    font-size: 10.5px;
    color: var(--ink-3);
  }

  .s-title {
    font-size: 12.5px;
    color: var(--ink);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .s-count {
    font-family: var(--mono);
    font-size: 10.5px;
    color: var(--ink-3);
  }
}

.empty {
  padding: 52px 0;
  text-align: center;
  font-size: 12.5px;
  letter-spacing: 0.08em;
  color: var(--ink-3);
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

  tbody tr {
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.5);
    }
  }
}

.foot {
  margin-top: 52px;
  padding: 18px 0 6px;
  border-top: 1px solid var(--line-2);
  display: flex;
  justify-content: space-between;
  font-family: var(--mono);
  font-size: 10px;
  letter-spacing: 0.08em;
  color: var(--ink-3);
  animation: rise 0.8s var(--ease) 0.3s both;
}

@media (max-width: 1100px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero-art {
    height: 200px;
  }

  .metrics {
    grid-template-columns: 1fr 1fr;
  }

  .grid-2 {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero,
  .block,
  .grid-2,
  .card.last,
  .foot {
    animation: none;
  }
}
</style>
