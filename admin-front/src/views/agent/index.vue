<template>
  <div class="agent-workbench">
    <!-- 左：会话列表 -->
    <aside class="sessions-panel">
      <div class="panel-title">
        <span>会话</span>
        <span class="panel-count">{{ sessions.length }}</span>
      </div>
      <button class="new-session-btn" @click="newSession">
        <el-icon><Plus /></el-icon>
        新会话
      </button>
      <div
        v-for="s in sessions"
        :key="s.id"
        class="session-item"
        :class="{ active: s.id === sessionId }"
        @click="switchSession(s.id)"
      >
        <el-icon><ChatDotRound /></el-icon>
        <span class="session-title">{{ s.title }}</span>
      </div>
    </aside>

    <!-- 中：对话流 -->
    <main class="chat-panel">
      <div class="chat-header">
        <div class="chat-title">
          <h3>医疗诊断工作台</h3>
          <span class="chat-sub">会话 {{ sessionShortId }} · 输出仅作医学信息参考</span>
        </div>
        <el-tag v-if="mockMode === true" type="warning" size="small" effect="plain">演示模式 · 未配置 LLM Key</el-tag>
        <el-tag v-else-if="mockMode === false" type="success" size="small" effect="plain">DeepSeek 已接入</el-tag>
        <el-tag v-else type="info" size="small" effect="plain">检测模型状态…</el-tag>
      </div>

      <div class="chat-scroll" ref="scrollRef">
        <div v-if="!messages.length" class="chat-empty">
          <div class="empty-mark">
            <el-icon :size="30"><FirstAidKit /></el-icon>
          </div>
          <h4>开始一次医疗问诊</h4>
          <p>描述症状、用药或诊疗问题，回答会附上检索证据与免责声明。</p>
          <div class="suggestions">
            <button
              v-for="s in suggestions"
              :key="s"
              type="button"
              class="suggestion"
              @click="useSuggestion(s)"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <div v-for="(msg, i) in messages" :key="i" class="msg-row" :class="msg.role">
          <span class="msg-avatar" :class="msg.role === 'user' ? 'me' : 'ai'">
            <el-icon v-if="msg.role === 'assistant'"><FirstAidKit /></el-icon>
            <template v-else>我</template>
          </span>
          <div class="msg-bubble">
            <div v-if="msg.role === 'assistant'" class="msg-content" v-html="renderMarkdown(msg.content)"></div>
            <div v-else class="msg-content">{{ msg.content }}</div>
            <!-- 证据角标 -->
            <div v-if="msg.evidence?.length" class="evidence-list">
              <div v-for="ev in msg.evidence" :key="ev.refId" class="evidence-card">
                <span class="ev-ref">{{ ev.refId }}</span>
                <div class="ev-body">
                  <div class="ev-title">{{ ev.title }}
                    <el-tag size="small" :type="ev.type === 'drug' ? 'success' : 'primary'" effect="plain">
                      {{ ev.type === 'drug' ? '药品库' : '知识库' }}
                    </el-tag>
                    <span v-if="ev.page" class="ev-page">第{{ ev.page }}页</span>
                  </div>
                  <div class="ev-snippet">{{ ev.snippet.slice(0, 120) }}{{ ev.snippet.length > 120 ? '…' : '' }}</div>
                </div>
              </div>
            </div>
            <!-- 风险确认卡 -->
            <div v-if="msg.riskAsk" class="risk-card">
              <div class="risk-title">⚠️ 高风险内容确认</div>
              <div class="risk-msg">{{ msg.riskAsk.message }}</div>
              <el-button type="warning" size="small" @click="confirmRisk(msg.riskAsk.confirmToken)">我已知晓，继续生成</el-button>
              <el-button size="small" text @click="skipRisk(msg)">跳过，不生成</el-button>
            </div>
          </div>
        </div>

        <div v-if="streaming" class="msg-row assistant">
          <div class="msg-bubble typing">正在生成<span class="dots"><i></i><i></i><i></i></span></div>
        </div>
      </div>

      <div class="disclaimer-bar" v-if="lastDisclaimer">{{ lastDisclaimer }}</div>

      <div class="input-bar">
        <div class="composer">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            resize="none"
            maxlength="2000"
            placeholder="描述症状 / 药品 / 诊疗问题…"
            @keydown.ctrl.enter.prevent="send"
          />
          <div class="input-hint">
            <span>Ctrl + Enter 发送</span>
            <span>{{ input.length }} / 2000</span>
          </div>
        </div>
        <el-button type="primary" class="send-btn" :disabled="streaming || !input.trim()" @click="send">
          <el-icon><Promotion /></el-icon>
          发送
        </el-button>
      </div>
    </main>

    <!-- 右：会话概览 -->
    <aside class="profile-panel">
      <div class="profile-header">会话概览</div>
      <div class="overview-list">
        <div class="overview-item">
          <span class="ov-label">当前会话</span>
          <span class="ov-value mono">{{ sessionShortId }}</span>
        </div>
        <div class="overview-item">
          <span class="ov-label">消息数</span>
          <span class="ov-value">{{ messages.length }}</span>
        </div>
        <div class="overview-item">
          <span class="ov-label">检索来源</span>
          <span class="ov-value">向量库 + BM25 + 药品库</span>
        </div>
      </div>

      <div class="profile-header sub">安全机制</div>
      <ul class="safety-list">
        <li><el-icon><CircleCheck /></el-icon>高风险问题二次确认</li>
        <li><el-icon><CircleCheck /></el-icon>回答附带证据引用</li>
        <li><el-icon><CircleCheck /></el-icon>固定免责声明</li>
      </ul>

      <div class="profile-note">会话与审计已落库，默认保留 90 天；手机号等敏感信息入库前脱敏。</div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { chatStream, confirmStream, listSessions, getMessages, getStatus, rejectRisk } from '@/api/agent'

const sessionId = ref(crypto.randomUUID())
const sessions = ref([{ id: sessionId.value, title: '新会话' }])
const messages = ref([])
const input = ref('')
const streaming = ref(false)
const mockMode = ref(false)
const lastDisclaimer = ref('')
const scrollRef = ref(null)

const sessionShortId = computed(() => (sessionId.value || '').slice(0, 8))

const suggestions = [
  '布洛芬一次吃几片？',
  '感冒发烧怎么处理？',
  '头孢克洛胶囊的适应症是什么？'
]

function useSuggestion(text) {
  if (streaming.value) return
  input.value = text
  send()
}

// 待确认时暂存 confirmToken
let pendingToken = null
// 当前流式请求的取消句柄：切会话/重新提问/离开页面时中断，避免串流
let controller = null

function abortStream() {
  if (controller) {
    controller.abort()
    controller = null
  }
}

onUnmounted(abortStream)

function newSession() {
  abortStream()
  sessionId.value = crypto.randomUUID()
  sessions.value.unshift({ id: sessionId.value, title: '新会话' })
  messages.value = []
  pendingToken = null
}

function switchSession(id) {
  abortStream()
  sessionId.value = id
  pendingToken = null
  loadMessages(id)
}

// 从后端加载历史会话；当前本地会话若还没落库则保留在最前
async function loadSessions() {
  try {
    const res = await listSessions(50)
    const remote = (res?.data || []).map((s) => ({
      id: s.sessionId,
      title: s.title || '未命名会话',
      updatedAt: s.updatedAt,
    }))
    const local = sessions.value.filter((s) => !remote.some((r) => r.id === s.id))
    sessions.value = [...local, ...remote]
  } catch (e) {
    // 历史会话不可用时降级为本地会话，不影响当前问诊
  }
}

// 切换会话时从库中回放消息
async function loadMessages(id) {
  try {
    const res = await getMessages(id)
    messages.value = (res?.data || []).map((m) => ({
      role: m.role,
      content: m.content,
      evidence: [],
      done: m.role === 'assistant',
    }))
    scrollToBottom()
  } catch (e) {
    messages.value = []
  }
}

onMounted(() => {
  loadSessions()
  loadStatus()
})

async function loadStatus() {
  try {
    const res = await getStatus()
    mockMode.value = !!res?.data?.mock
  } catch (e) {
    // 状态不可用时保持“检测中”，不阻塞问诊
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  })
}

function ensureAssistant() {
  let last = messages.value[messages.value.length - 1]
  if (!last || last.role !== 'assistant' || last.done) {
    last = { role: 'assistant', content: '', evidence: [] }
    messages.value.push(last)
  }
  return last
}

async function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''
  pendingToken = null
  messages.value.push({ role: 'user', content: text })
  streaming.value = true
  scrollToBottom()
  abortStream()
  controller = new AbortController()

  try {
    await chatStream({
      sessionId: sessionId.value,
      message: text,
      signal: controller.signal,
      onEvent: handleEvent,
    })
  } catch (e) {
    if (e.name !== 'AbortError') ElMessage.error('连接失败：' + e.message)
  } finally {
    controller = null
    streaming.value = false
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') last.done = true
    scrollToBottom()
  }
}

async function skipRisk(msg) {
  try {
    await rejectRisk(sessionId.value)
  } catch (e) {
    // 后端清理失败不阻塞前端
  }
  pendingToken = null
  msg.riskAsk = null
  msg.content = '已跳过本次高风险内容生成。如需了解剂量或处方，请咨询医生或药师。'
  msg.done = true
  ElMessage.info('已跳过，未生成剂量或处方类内容')
}

async function confirmRisk(token) {  streaming.value = true
  scrollToBottom()
  abortStream()
  controller = new AbortController()
  try {
    await confirmStream({
      sessionId: sessionId.value,
      confirmToken: token,
      signal: controller.signal,
      onEvent: handleEvent,
    })
  } catch (e) {
    if (e.name !== 'AbortError') ElMessage.error('确认失败：' + e.message)
  } finally {
    controller = null
    streaming.value = false
    scrollToBottom()
  }
}

function handleEvent(ev) {
  if (ev.type === 'start') {
    mockMode.value = !!ev.data?.mock
    ensureAssistant()
  } else if (ev.type === 'evidence') {
    const a = ensureAssistant()
    a.evidence = ev.data?.items || []
  } else if (ev.type === 'delta') {
    const a = ensureAssistant()
    a.content += ev.data?.text || ''
    scrollToBottom()
  } else if (ev.type === 'risk') {
    // 确认卡追加为一条助手消息
    messages.value.push({
      role: 'assistant',
      content: '',
      riskAsk: ev.data,
    })
    pendingToken = ev.data?.confirmToken || null
    scrollToBottom()
  } else if (ev.type === 'done') {
    if (ev.data?.disclaimer) lastDisclaimer.value = ev.data.disclaimer
    const a = messages.value[messages.value.length - 1]
    if (a && a.role === 'assistant') a.done = true
    // 用首条用户消息更新会话标题
    const s = sessions.value.find((x) => x.id === sessionId.value)
    if (s && s.title === '新会话') {
      const firstUser = messages.value.find((m) => m.role === 'user')
      if (firstUser) s.title = firstUser.content.slice(0, 14)
    }
    loadSessions()
  } else if (ev.type === 'error') {
    ElMessage.error(ev.data?.message || '服务异常')
  }
}

// 轻量 markdown 渲染（标题/加粗/换行/引用角标高亮）
function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\[(\d+)\]/g, '<sup class="ref">$1</sup>')
    .replace(/\*\*(.+?)\*\*/g, '<b>$1</b>')
    .replace(/^#{1,6}\s+(.+)$/gm, '<div class="md-h">$1</div>')
    .replace(/\n/g, '<br/>')
}
</script>

<style lang="scss" scoped>
.agent-workbench {
  display: flex;
  height: calc(100vh - 60px - 48px);
  min-height: 520px;
  gap: 14px;
}

// 左侧会话
.sessions-panel {
  width: 230px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(16px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow: auto;

  .panel-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 2px 4px 8px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.4px;
    color: var(--aimin-text-tertiary, #a1a1a6);
  }

  .panel-count {
    padding: 1px 7px;
    border-radius: 999px;
    background: rgba(15, 23, 42, 0.06);
    font-size: 11px;
    color: var(--aimin-text-secondary, #6e6e73);
  }

  .new-session-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px;
    border: none;
    border-radius: 12px;
    background: var(--aimin-accent, #0a84ff);
    color: #fff;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.12s;
    &:active {
      transform: scale(0.97);
    }
  }

  .session-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    color: var(--aimin-text-secondary, #6e6e73);
    transition: background 0.18s;
    &:hover {
      background: rgba(0, 0, 0, 0.04);
    }
    &.active {
      background: rgba(10, 132, 255, 0.12);
      color: var(--aimin-accent, #0a84ff);
      font-weight: 600;
    }
    .session-title {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 13px;
    }
  }
}

// 中间对话
.chat-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;

  .chat-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    .chat-title {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 700;
    }
    .chat-sub {
      font-size: 11.5px;
      color: var(--aimin-text-tertiary, #a1a1a6);
    }
  }

  .chat-scroll {
    flex: 1;
    overflow: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .chat-empty {
    margin: auto;
    padding: 20px;
    max-width: 460px;
    text-align: center;

    .empty-mark {
      width: 64px;
      height: 64px;
      margin: 0 auto 16px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      background: var(--aimin-gradient-brand, linear-gradient(135deg, #0a84ff, #38bdf8));
      box-shadow: 0 16px 32px -18px rgba(10, 132, 255, 0.9);
    }

    h4 {
      font-size: 17px;
      font-weight: 700;
      margin-bottom: 6px;
    }

    p {
      font-size: 12.5px;
      line-height: 1.7;
      color: var(--aimin-text-secondary, #6e6e73);
    }
  }

  .suggestions {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    margin-top: 18px;
  }

  .suggestion {
    padding: 8px 14px;
    border: 1px solid rgba(10, 132, 255, 0.18);
    border-radius: 999px;
    background: rgba(10, 132, 255, 0.06);
    color: var(--aimin-accent, #0a84ff);
    font-size: 12.5px;
    cursor: pointer;
    transition: background 0.18s ease, border-color 0.18s ease,
      transform 0.18s var(--aimin-ease);

    &:hover {
      background: rgba(10, 132, 255, 0.12);
      border-color: rgba(10, 132, 255, 0.35);
      transform: translateY(-1px);
    }
  }

  .msg-row {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    &.user {
      flex-direction: row-reverse;
      justify-content: flex-start;
      .msg-bubble {
        background: var(--aimin-accent, #0a84ff);
        color: #fff;
        border-radius: 16px 16px 4px 16px;
        box-shadow: 0 8px 20px -14px rgba(10, 132, 255, 0.9);
      }
    }
    &.assistant {
      justify-content: flex-start;
      .msg-bubble {
        background: #fff;
        border-radius: 16px 16px 16px 4px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
      }
    }
  }

  .msg-avatar {
    width: 30px;
    height: 30px;
    flex-shrink: 0;
    margin-top: 2px;
    border-radius: 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;

    &.ai {
      background: var(--aimin-gradient-brand, linear-gradient(135deg, #0a84ff, #38bdf8));
      color: #fff;
      font-size: 14px;
      box-shadow: 0 6px 14px -8px rgba(10, 132, 255, 0.9);
    }

    &.me {
      background: rgba(15, 23, 42, 0.06);
      color: var(--aimin-text-secondary, #6e6e73);
    }
  }

  .msg-bubble {
    max-width: 78%;
    padding: 12px 16px;
    line-height: 1.65;
    .msg-content :deep(sup.ref) {
      color: var(--aimin-accent, #0a84ff);
      font-weight: 700;
    }
    .msg-content :deep(.md-h) {
      font-weight: 700;
      margin: 6px 0 2px;
    }
  }

  // 证据卡
  .evidence-list {
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }
  .evidence-card {
    display: flex;
    gap: 8px;
    padding: 8px 10px;
    background: #f7f9fc;
    border: 1px solid #e8eef7;
    border-radius: 10px;
    .ev-ref {
      color: var(--aimin-accent, #0a84ff);
      font-weight: 700;
      font-size: 12px;
    }
    .ev-title {
      font-size: 12px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .ev-page {
      color: var(--aimin-text-secondary, #6e6e73);
      font-weight: 400;
    }
    .ev-snippet {
      font-size: 12px;
      color: var(--aimin-text-secondary, #6e6e73);
      margin-top: 3px;
      line-height: 1.5;
    }
  }

  // 风险确认卡（琥珀色警示）
  .risk-card {
    margin-top: 8px;
    padding: 12px 14px;
    background: #fff8eb;
    border: 1px solid #f5d48a;
    border-radius: 12px;
    .risk-title {
      font-weight: 700;
      color: #9a6700;
      margin-bottom: 6px;
    }
    .risk-msg {
      font-size: 13px;
      color: #7a5a00;
      margin-bottom: 10px;
    }
  }
}

.disclaimer-bar {
  text-align: center;
  font-size: 11px;
  color: var(--aimin-text-secondary, #6e6e73);
  padding: 4px;
  background: rgba(0, 0, 0, 0.02);
}

.input-bar {
  display: flex;
  gap: 10px;
  padding: 12px 14px 14px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  align-items: flex-end;
  background: rgba(255, 255, 255, 0.55);

  .composer {
    flex: 1;
    min-width: 0;
  }

  .input-hint {
    display: flex;
    justify-content: space-between;
    padding: 5px 4px 0;
    font-size: 11px;
    color: var(--aimin-text-tertiary, #a1a1a6);
  }

  .send-btn {
    height: 54px;
    padding: 0 22px;
    font-weight: 600;
  }
}

// 右侧概览
.profile-panel {
  width: 248px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(16px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 18px 16px;
  display: flex;
  flex-direction: column;

  .profile-header {
    font-size: 13px;
    font-weight: 700;
    margin-bottom: 12px;

    &.sub {
      margin-top: 22px;
    }
  }

  .overview-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .overview-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 9px 11px;
    border-radius: 10px;
    background: rgba(15, 23, 42, 0.03);
    font-size: 12px;
  }

  .ov-label {
    flex: 0 0 auto;
    color: var(--aimin-text-secondary, #6e6e73);
  }

  .ov-value {
    margin-left: 10px;
    text-align: right;
    line-height: 1.3;
    font-weight: 600;
    color: var(--aimin-text, #1d1d1f);

    &.mono {
      font-family: 'SF Mono', 'JetBrains Mono', Consolas, monospace;
      font-size: 11.5px;
      letter-spacing: 0.4px;
    }
  }

  .safety-list {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 9px;

    li {
      display: flex;
      align-items: center;
      gap: 7px;
      font-size: 12px;
      color: var(--aimin-text-secondary, #6e6e73);

      .el-icon {
        color: #12b76a;
        font-size: 13px;
      }
    }
  }

  .profile-note {
    margin-top: auto;
    padding: 12px;
    border-radius: 10px;
    background: rgba(10, 132, 255, 0.06);
    font-size: 11.5px;
    line-height: 1.6;
    color: var(--aimin-text-secondary, #6e6e73);
  }
}

@media (max-width: 1240px) {
  .profile-panel {
    display: none;
  }
}

@media (max-width: 980px) {
  .sessions-panel {
    display: none;
  }
}

// 打字动画
.typing .dots {
  display: inline-flex;
  gap: 3px;
  margin-left: 4px;
  i {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: var(--aimin-text-secondary, #6e6e73);
    animation: blink 1.2s infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}
@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; }
  40% { opacity: 1; }
}
</style>
