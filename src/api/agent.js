import request from '@/utils/request'
import { useUserStore } from '@/store'

// SSE 与普通接口统一走 /api：开发环境由 Vite 代理去掉前缀，生产由网关/nginx 转发
const API_BASE = import.meta.env.VITE_API_PREFIX || '/api'

function authHeaders() {
  const store = useUserStore()
  const name = store?.getTokenName
  const value = store?.getTokenValue
  return name && value ? { [name]: value } : {}
}

/** 读取 SSE 流：逐条把 data: {...} 解析成事件 */
async function streamSse(path, body, { signal, onEvent }) {
  const resp = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    signal,
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(body),
  })
  if (resp.status === 401) {
    const store = useUserStore()
    store.clearLoginState?.()
    throw new Error('登录状态已失效，请重新登录')
  }
  if (!resp.ok) {
    throw new Error(`HTTP ${resp.status}`)
  }
  if (!resp.body) {
    throw new Error('当前浏览器不支持流式响应')
  }
  const reader = resp.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      // SSE: 事件以 data: 开头，空行分隔
      const parts = buffer.split('\n\n')
      buffer = parts.pop()
      for (const part of parts) {
        const line = part.split('\n').find((l) => l.startsWith('data:'))
        if (!line) continue
        try {
          onEvent(JSON.parse(line.slice(5).trim()))
        } catch (e) {
          // 半包或非 JSON 数据，忽略
        }
      }
    }
  } finally {
    if (reader.releaseLock) reader.releaseLock()
  }
}

// 对话（SSE 流式，POST 不支持 EventSource，用 fetch 流读取）
export function chatStream({ sessionId, message, confirmToken, onEvent, signal }) {
  return streamSse('/aimin-agent/agent/chat', { sessionId, message, confirmToken }, { signal, onEvent })
}

// 风险确认（两阶段第二阶段）
export function confirmStream({ sessionId, confirmToken, onEvent, signal }) {
  return streamSse('/aimin-agent/agent/confirm', { sessionId, confirmToken }, { signal, onEvent })
}

// 上传知识文档（axios 实例的 baseURL 是后台前缀，这里显式覆盖为 /api）
export function uploadDoc(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request({
    baseURL: API_BASE,
    url: '/aimin-agent/agent/docs',
    method: 'post',
    data: fd,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 已入库文档列表
export function listDocs() {
  return request({ baseURL: API_BASE, url: '/aimin-agent/agent/docs', method: 'get' })
}

// 历史会话列表
export function listSessions(limit = 50) {
  return request({ baseURL: API_BASE, url: '/aimin-agent/agent/sessions', method: 'get', params: { limit } })
}

// 单个会话的历史消息
export function getMessages(sessionId) {
  return request({ baseURL: API_BASE, url: `/aimin-agent/agent/sessions/${sessionId}/messages`, method: 'get' })
}

// 放弃高风险问题（清待确认项并记审计）
export function rejectRisk(sessionId) {
  return request({ baseURL: API_BASE, url: '/aimin-agent/agent/risk/reject', method: 'post', data: { sessionId } })
}
