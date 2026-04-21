import { buildApiUrl } from './http'

export function openLoveChatSse({ message, chatId, onMessage, onError, onDone }) {
  const url = buildApiUrl('/ai/love_app/chat/sse', { message, chatId })
  return openSseStream({ url, onMessage, onError, onDone })
}

export function openManusChatSse({ message, onMessage, onError, onDone }) {
  const url = buildApiUrl('/ai/manus/chat', { message })
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
