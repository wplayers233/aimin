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
<style scoped lang="scss">
.agent-workbench {
  display: grid;
  grid-template-columns: 214px minmax(0, 1fr) 262px;
  height: calc(100vh - 146px);
  border: 1px solid var(--line);
  border-radius: 4px;
  overflow: hidden;
  background: transparent;
}

.mono {
  font-family: var(--mono);
  font-variant-numeric: tabular-nums;
}

/* 左：会话 */
.sessions-panel {
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow-y: auto;

  .panel-title {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    padding: 15px 18px 12px;
    border-bottom: 1px solid var(--line);
    font-family: var(--mono);
    font-size: 10px;
    letter-spacing: 0.16em;
    color: var(--ink-3);
    text-transform: uppercase;
  }

  .panel-count {
    font-family: var(--mono);
    font-size: 10px;
    color: var(--ink-3);
  }

  .new-session-btn {
    margin: 12px 14px;
    height: 34px;
    border: 1px solid var(--line);
    border-radius: 2px;
    background: transparent;
    color: var(--ink-2);
    font-size: 12.5px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    cursor: pointer;
    transition: border-color 0.25s, color 0.25s;

    &:hover {
      border-color: var(--ink);
      color: var(--ink);
    }
  }

  .session-item {
    position: relative;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 11px 18px;
    border-bottom: 1px solid var(--line-2);
    color: var(--ink-2);
    font-size: 12.5px;
    cursor: pointer;
    transition: background 0.2s, color 0.2s;

    &:hover {
      background: rgba(22, 32, 46, 0.025);
      color: var(--ink);
    }

    &.active {
      background: rgba(42, 74, 107, 0.06);
      color: var(--ink);

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 2px;
        background: var(--ochre);
      }
    }

    .session-title {
      flex: 1;
      min-width: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    :deep(.el-icon) {
      color: var(--ink-3);
      font-size: 13px;
    }
  }
}

/* 中：对话 */
.chat-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 24px;
  border-bottom: 1px solid var(--line);

  .chat-title h3 {
    font-family: var(--serif);
    font-size: 17px;
    font-weight: 400;
    letter-spacing: 0.01em;
  }

  .chat-sub {
    display: block;
    margin-top: 2px;
    font-family: var(--mono);
    font-size: 9.5px;
    letter-spacing: 0.08em;
    color: var(--ink-3);
  }

  :deep(.el-tag) {
    background: transparent;
    border-color: var(--line);
    color: var(--ink-3);
    font-family: var(--mono);
    font-size: 10px;
    letter-spacing: 0.06em;
  }
}

.chat-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 24px 26px;
  min-height: 0;
}

.chat-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;

  .empty-mark {
    width: 52px;
    height: 52px;
    border: 1px solid var(--line);
    border-radius: 50%;
    display: grid;
    place-items: center;
    color: var(--ink-3);
    margin-bottom: 16px;
  }

  h4 {
    font-family: var(--serif);
    font-size: 19px;
    font-weight: 400;
  }

  p {
    margin-top: 6px;
    font-size: 12.5px;
    color: var(--ink-2);
    max-width: 42ch;
  }

  .suggestions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
    margin-top: 18px;
  }

  .suggestion {
    border: 1px solid var(--line);
    border-radius: 2px;
    background: transparent;
    color: var(--ink-2);
    font-size: 12px;
    padding: 6px 12px;
    cursor: pointer;
    transition: border-color 0.2s, color 0.2s;

    &:hover {
      border-color: var(--ink);
      color: var(--ink);
    }
  }
}

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;

  &.user {
    flex-direction: row-reverse;
  }

  .msg-avatar {
    width: 26px;
    height: 26px;
    flex: 0 0 26px;
    border-radius: 50%;
    border: 1px solid var(--line);
    display: grid;
    place-items: center;
    font-size: 10.5px;
    color: var(--ink-3);
    margin-top: 2px;

    &.ai {
      background: var(--indigo);
      border-color: var(--indigo);
      color: var(--paper);
    }
  }

  .msg-bubble {
    max-width: 76%;
    min-width: 0;
  }

  &.user .msg-bubble {
    background: rgba(42, 74, 107, 0.07);
    border-radius: 2px;
    padding: 9px 13px;
  }

  .msg-content {
    font-size: 13.5px;
    line-height: 1.85;
    color: var(--ink);
    word-break: break-word;

    :deep(p) {
      margin: 0 0 8px;
    }

    :deep(p:last-child) {
      margin-bottom: 0;
    }

    :deep(ul),
    :deep(ol) {
      padding-left: 18px;
      margin: 6px 0;
    }

    :deep(blockquote) {
      border-left: 2px solid var(--ochre);
      padding-left: 10px;
      color: var(--ink-2);
      margin: 8px 0;
    }

    :deep(code) {
      font-family: var(--mono);
      font-size: 12px;
      background: rgba(22, 32, 46, 0.05);
      padding: 1px 4px;
      border-radius: 2px;
    }
  }

  .msg-bubble.typing {
    color: var(--ink-3);
    font-size: 12.5px;

    .dots {
      display: inline-flex;
      gap: 3px;
      margin-left: 6px;

      i {
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: var(--ink-3);
        animation: blink 1.2s infinite;

        &:nth-child(2) {
          animation-delay: 0.2s;
        }
        &:nth-child(3) {
          animation-delay: 0.4s;
        }
      }
    }
  }
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; }
  40% { opacity: 1; }
}

.evidence-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 10px;
}

.evidence-card {
  display: flex;
  gap: 9px;
  padding: 9px 11px 9px 9px;
  border: 1px solid var(--line);
  border-left: 2px solid var(--indigo);
  border-radius: 2px;

  &:has(.el-tag--success) {
    border-left-color: var(--ochre);
  }

  .ev-ref {
    font-family: var(--mono);
    font-size: 10px;
    color: var(--paper);
    background: var(--indigo);
    border-radius: 1px;
    padding: 2px 6px;
    height: 18px;
    line-height: 14px;
    flex: 0 0 auto;
    letter-spacing: 0;
  }

  .ev-body {
    min-width: 0;
  }

  .ev-title {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    font-size: 11.5px;
    color: var(--ink);
  }

  :deep(.el-tag) {
    height: 16px;
    line-height: 14px;
    padding: 0 5px;
    border-color: var(--line);
    color: var(--ink-3);
    background: transparent;
    font-family: var(--mono);
    font-size: 9.5px;
  }

  .ev-page {
    font-family: var(--mono);
    font-size: 9.5px;
    color: var(--ink-3);
  }

  .ev-snippet {
    margin-top: 3px;
    font-size: 11.5px;
    line-height: 1.65;
    color: var(--ink-2);
  }
}

.risk-card {
  margin-top: 10px;
  padding: 12px 14px;
  border: 1px solid rgba(180, 85, 45, 0.4);
  background: var(--ochre-soft);
  border-radius: 2px;

  .risk-title {
    font-size: 12px;
    font-weight: 500;
    color: #7a3a1e;
  }

  .risk-msg {
    margin: 4px 0 10px;
    font-size: 12px;
    line-height: 1.7;
    color: #7a3a1e;
  }

  :deep(.el-button) {
    margin-right: 8px;
    border-radius: 2px;
  }

  :deep(.el-button--warning) {
    background: var(--ochre);
    border-color: var(--ochre);
    color: #fff;
  }
}

.disclaimer-bar {
  padding: 7px 24px;
  border-top: 1px solid var(--line);
  background: rgba(22, 32, 46, 0.025);
  font-family: var(--mono);
  font-size: 9.5px;
  letter-spacing: 0.04em;
  color: var(--ink-3);
}

.input-bar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 20px 14px;
  border-top: 1px solid var(--line);

  .composer {
    flex: 1;
    min-width: 0;

    :deep(.el-textarea__inner) {
      box-shadow: none;
      border-radius: 0;
      background: transparent;
      padding: 6px 0;
      font-size: 13.5px;
      line-height: 1.7;
      color: var(--ink);

      &:focus {
        box-shadow: none;
      }
    }
  }

  .input-hint {
    display: flex;
    justify-content: space-between;
    font-family: var(--mono);
    font-size: 9.5px;
    letter-spacing: 0.06em;
    color: var(--ink-3);
    margin-top: 2px;
  }

  .send-btn {
    height: 38px;
    padding: 0 18px;
    border-radius: 2px;
    background: var(--ink);
    border-color: var(--ink);
    color: var(--paper);
    font-size: 12.5px;
    letter-spacing: 0.04em;

    &:hover:not(.is-disabled) {
      background: var(--indigo);
      border-color: var(--indigo);
    }

    &.is-disabled {
      background: var(--paper-3);
      border-color: var(--paper-3);
      color: var(--ink-3);
    }
  }
}

/* 右：会话概览 */
.profile-panel {
  border-left: 1px solid var(--line);
  padding: 16px 18px;
  overflow-y: auto;
  font-size: 12px;

  .profile-header {
    font-family: var(--mono);
    font-size: 10px;
    letter-spacing: 0.16em;
    color: var(--ink-3);
    text-transform: uppercase;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--line);

    &.sub {
      margin-top: 22px;
    }
  }

  .overview-list {
    margin-top: 4px;
  }

  .overview-item {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 10px;
    padding: 9px 0;
    border-bottom: 1px solid var(--line-2);

    .ov-label {
      color: var(--ink-2);
      font-size: 11.5px;
      white-space: nowrap;
    }

    .ov-value {
      color: var(--ink);
      font-size: 11.5px;
      text-align: right;
    }
  }

  .safety-list {
    list-style: none;
    margin-top: 8px;
    padding: 0;

    li {
      display: flex;
      align-items: center;
      gap: 7px;
      padding: 5px 0;
      color: var(--ink-2);
      font-size: 11.5px;

      :deep(.el-icon) {
        color: var(--sage);
        font-size: 12px;
      }
    }
  }

  .profile-note {
    margin-top: 18px;
    padding-top: 12px;
    border-top: 1px solid var(--line);
    font-size: 11px;
    line-height: 1.7;
    color: var(--ink-3);
  }
}

@media (max-width: 1200px) {
  .agent-workbench {
    grid-template-columns: 200px minmax(0, 1fr);
  }
  .profile-panel {
    display: none;
  }
}

/* ---- 交互增强 ---- */
.msg-row {
  animation: msg-in 0.45s var(--ease) both;
}

@keyframes msg-in {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: none; }
}

.sessions-panel .session-item {
  transition: background 0.2s, color 0.2s, padding-left 0.25s var(--ease);

  &:hover {
    padding-left: 22px;
  }
}

.evidence-card {
  transition: transform 0.25s var(--ease), border-color 0.25s, background 0.25s;

  &:hover {
    transform: translateX(2px);
    border-color: rgba(36, 65, 95, 0.28);
    background: rgba(255, 255, 255, 0.22);
  }
}

.risk-card {
  animation: risk-in 0.5s var(--ease) both;
}

@keyframes risk-in {
  0% { opacity: 0; transform: scale(0.985); border-color: rgba(169, 73, 31, 0.9); }
  60% { border-color: rgba(169, 73, 31, 0.9); }
  100% { opacity: 1; transform: none; border-color: rgba(169, 73, 31, 0.4); }
}

.chat-empty .suggestion {
  transition: border-color 0.2s, color 0.2s, background 0.2s, transform 0.25s var(--ease);

  &:hover {
    background: rgba(255, 255, 255, 0.24);
    transform: translateY(-1px);
  }
}

.send-btn {
  transition: transform 0.25s var(--ease), background 0.25s;

  &:hover:not(.is-disabled) {
    transform: translateY(-1px);
  }
}

.new-session-btn {
  transition: border-color 0.25s, color 0.25s, background 0.25s;

  &:hover {
    background: rgba(255, 255, 255, 0.24);
  }
}


/* ---- 色彩层次收尾 ---- */
.chat-header {
  background: linear-gradient(90deg, rgba(36, 65, 95, 0.06), transparent 62%);
}

.profile-panel .profile-header::before {
  content: '';
  display: inline-block;
  width: 2px;
  height: 10px;
  background: var(--ochre);
  margin-right: 7px;
  vertical-align: -1px;
}

.evidence-card :deep(.el-tag--primary) {
  color: var(--indigo);
  border-color: rgba(36, 65, 95, 0.3);
}

.evidence-card :deep(.el-tag--success) {
  color: var(--ochre);
  border-color: rgba(169, 73, 31, 0.35);
}

</style>
