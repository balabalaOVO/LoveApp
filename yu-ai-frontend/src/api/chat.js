import { apiClient, buildApiUrl } from './http'
import { getAuthToken } from '../store/auth'

const AUTH_STORAGE_KEY = 'yu_ai_auth'

function resolveToken() {
  const token = getAuthToken()
  if (token) {
    return token
  }
  try {
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed.token) {
        return parsed.token
      }
    }
  } catch {
    // ignore parse errors
  }
  return ''
}

export function openLoveChatSse({ message, chatId, onMessage, onError, onDone }) {
  const token = resolveToken()
  if (!token) {
    onError?.('未登录或登录已过期，请重新登录。')
    return { close: () => {} }
  }
  const url = buildApiUrl('/ai/love_app/chat/sse', { message, chatId, token })
  return openSseStream({ url, onMessage, onError, onDone })
}

export function openManusChatSse({ message, onMessage, onError, onDone }) {
  const token = resolveToken()
  if (!token) {
    onError?.('未登录或登录已过期，请重新登录。')
    return { close: () => {} }
  }
  const url = buildApiUrl('/ai/manus/chat', { message, token })
  return openSseStream({ url, onMessage, onError, onDone })
}

function openSseStream({ url, onMessage, onError, onDone }) {
  const source = new EventSource(url)

  source.onmessage = (event) => {
    const chunk = event.data ?? ''
    if (chunk === '[DONE]' || chunk === 'DONE') {
      source.close()
      onDone?.()
      return
    }
    onMessage?.(chunk)
  }

  source.onerror = () => {
    source.close()
    onError?.('连接已中断，请稍后重试。')
  }

  return {
    close: () => source.close()
  }
}

export async function listConversations(appType = 'love_app') {
  const response = await apiClient.get('/ai/conversations', { params: { appType } })
  return response.data ?? []
}

export async function getConversationMessages(chatKey) {
  const response = await apiClient.get(`/ai/conversations/${encodeURIComponent(chatKey)}/messages`)
  return response.data ?? []
}

export async function deleteConversation(chatKey) {
  await apiClient.delete(`/ai/conversations/${encodeURIComponent(chatKey)}`)
}
