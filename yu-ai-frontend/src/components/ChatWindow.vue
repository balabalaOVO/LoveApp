<script setup>
import { nextTick, onBeforeUnmount, ref } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入消息...'
  },
  chatId: {
    type: String,
    default: ''
  },
  canSend: {
    type: Boolean,
    default: true
  },
  blockedHint: {
    type: String,
    default: ''
  },
  streamRequest: {
    type: Function,
    required: true
  },
  responseMode: {
    type: String,
    default: 'single-bubble'
  },
  assistantAvatar: {
    type: String,
    default: ''
  },
  assistantAvatarAlt: {
    type: String,
    default: 'AI 头像'
  }
})

const messages = ref([])
const inputText = ref('')
const isSending = ref(false)
const messageListRef = ref(null)
const activeAssistantId = ref('')
const pendingThought = ref('')
const stepCounter = ref(0)
const activeStepMessageId = ref('')
const isCapturingStepThought = ref(false)
let streamController = null

function pushUserMessage(content) {
  messages.value.push({
    id: crypto.randomUUID(),
    role: 'user',
    content
  })
}

function pushAssistantMessage() {
  const draft = {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: '',
    kind: 'reply',
    loading: true
  }
  messages.value.push(draft)
  return draft
}

function buildAssistantMessage(segment) {
  return {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: segment.content,
    kind: segment.kind || 'reply',
    loading: false
  }
}

function isStepMode() {
  return props.responseMode === 'step-bubble'
}

function normalizePlainChunk(chunk) {
  if (chunk == null) {
    return ''
  }

  const text = String(chunk).replace(/\r\n/g, '\n')
  if (!text) {
    return ''
  }

  return text
    .split('\n')
    .map((line) => line.replace(/^\s*data:\s*/i, ''))
    .join('\n')
}

function normalizeChunkLines(chunk) {
  if (chunk == null) {
    return []
  }

  return String(chunk)
    .replace(/\r\n/g, '\n')
    .split('\n')
    .map((line) => {
      const withoutDataPrefix = line.replace(/^\s*data:\s*/i, '').trimEnd()
      return withoutDataPrefix.replace(
        /^\d{4}-\d{2}-\d{2}T[^\s]+\s+[A-Z]+\s+\d+\s+---\s+\[[^\]]*\]\s+\[[^\]]*\]\s+[^:]+:\s*/,
        ''
      )
    })
}

function isNoiseLine(line) {
  return (
    /^Executing step\s*\d+\/\d+/i.test(line) ||
    /^AI Request Context\s*:/i.test(line) ||
    /^AI Response\s*:?$/i.test(line) ||
    /选择了\s*\d+\s*个工具来使用/.test(line)
  )
}

function isStepLine(line) {
  return /^Step\s*\d+\s*[:：]/i.test(line)
}

function isToolResultLine(line) {
  return /工具\s*[A-Za-z0-9_]+\s*完成了它的任务！结果\s*[:：]/.test(line)
}

function extractStepNumber(text) {
  const match = text.match(/^Step\s*(\d+)\s*[:：]/i)
  if (!match) {
    return null
  }
  return Number(match[1])
}

function normalizeThoughtContent(text) {
  return text
    .replace(/^\s+|\s+$/g, '')
    .replace(/\n{3,}/g, '\n\n')
}

function splitStepLineAndThoughtStart(line) {
  const bracketStart = line.lastIndexOf('[')
  if (bracketStart === -1) {
    return null
  }

  return {
    stepPrefix: line.slice(0, bracketStart).trim(),
    thoughtSuffix: line.slice(bracketStart + 1).trim()
  }
}

function splitThoughtContentAndEndMark(text) {
  const trimmed = text.trim()
  if (!trimmed) {
    return {
      thoughtPart: '',
      ended: false
    }
  }

  if (trimmed.endsWith(']')) {
    return {
      thoughtPart: trimmed.slice(0, -1).trim(),
      ended: true
    }
  }

  return {
    thoughtPart: trimmed,
    ended: false
  }
}

function buildStepResultContent(toolLine) {
  const thought = normalizeThoughtContent(pendingThought.value)
  let base = toolLine.trim()

  if (!isStepLine(base)) {
    stepCounter.value += 1
    base = `Step ${stepCounter.value}: ${base}`
  } else {
    const stepNo = extractStepNumber(base)
    if (stepNo && stepNo > stepCounter.value) {
      stepCounter.value = stepNo
    }
  }

  if (!thought) {
    return base
  }

  pendingThought.value = ''
  return `${base} ${thought}`
}

function parseAssistantSegments(chunk) {
  const lines = normalizeChunkLines(chunk)
  if (!lines.length) {
    return []
  }

  const segments = []
  const marker = '的思考:'

  lines.forEach((rawLine) => {
    const line = rawLine.trim()
    if (!line) {
      if (pendingThought.value) {
        pendingThought.value = `${pendingThought.value}\n`
      }
      return
    }

    if (isNoiseLine(line)) {
      return
    }

    if (line === ']' || line === '[') {
      return
    }

    if (isCapturingStepThought.value) {
      const { thoughtPart, ended } = splitThoughtContentAndEndMark(line)
      const content = thoughtPart
      if (content) {
        segments.push({
          kind: 'append-step',
          content
        })
      }
      if (ended) {
        isCapturingStepThought.value = false
      }
      return
    }

    const thoughtIndex = line.indexOf(marker)
    if (thoughtIndex !== -1) {
      const thoughtStart = thoughtIndex + marker.length
      const thoughtLine = line.slice(thoughtStart).trim()
      if (thoughtLine) {
        pendingThought.value = pendingThought.value
          ? `${pendingThought.value}\n${thoughtLine}`
          : thoughtLine
      }
      return
    }

    if (pendingThought.value && !isToolResultLine(line) && !isStepLine(line)) {
      pendingThought.value = `${pendingThought.value}\n${line}`
      return
    }

    if (isToolResultLine(line)) {
      segments.push({
        kind: 'step',
        content: buildStepResultContent(line)
      })
      return
    }

    if (isStepLine(line)) {
      const stepNo = extractStepNumber(line)
      if (stepNo && stepNo > stepCounter.value) {
        stepCounter.value = stepNo
      }

      const stepWithThought = splitStepLineAndThoughtStart(line)
      if (stepWithThought) {
        const { stepPrefix, thoughtSuffix } = stepWithThought

        if (stepPrefix) {
          segments.push({
            kind: 'step',
            content: stepPrefix
          })
        }

        if (thoughtSuffix) {
          const { thoughtPart, ended } = splitThoughtContentAndEndMark(thoughtSuffix)
          if (thoughtPart) {
            segments.push({
              kind: 'append-step',
              content: thoughtPart
            })
          }
          isCapturingStepThought.value = !ended
        } else {
          isCapturingStepThought.value = true
        }
        return
      }

      segments.push({
        kind: 'step',
        content: line
      })
      return
    }

    segments.push({
      kind: 'reply',
      content: line
    })
  })

  return segments
}

function applySegmentToMessage(message, segment) {
  message.loading = false
  message.kind = segment.kind || 'reply'
  message.content = segment.content
}

function findMessageById(id) {
  if (!id) {
    return null
  }
  return messages.value.find((item) => item.id === id) || null
}

function appendToStepMessage(content, fallbackDraft) {
  if (!content) {
    return
  }

  let target = findMessageById(activeStepMessageId.value)
  if (!target) {
    target = findActiveAssistantDraft() || fallbackDraft
    if (!target) {
      return
    }
    target.loading = false
    target.kind = 'step'
    activeStepMessageId.value = target.id
  }

  target.content = target.content ? `${target.content}\n${content}` : content
}

function appendPlainChunk(chunk, fallbackDraft) {
  const draft = findActiveAssistantDraft() || fallbackDraft
  const text = normalizePlainChunk(chunk)
  if (!draft || !text) {
    return false
  }

  draft.loading = false
  draft.kind = 'reply'
  draft.content = `${draft.content || ''}${text}`
  return true
}

function appendAssistantSegments(chunk, fallbackDraft) {
  const segments = parseAssistantSegments(chunk)
  if (!segments.length) {
    return false
  }

  const draft = findActiveAssistantDraft() || fallbackDraft
  segments.forEach((segment) => {
    if (segment.kind === 'append-step') {
      appendToStepMessage(segment.content, fallbackDraft)
      return
    }

    if (segment.kind === 'reply' && findMessageById(activeStepMessageId.value)) {
      appendToStepMessage(segment.content, fallbackDraft)
      return
    }

    if (draft?.loading) {
      applySegmentToMessage(draft, segment)
      if (segment.kind === 'step') {
        activeStepMessageId.value = draft.id
      } else {
        activeStepMessageId.value = ''
      }
      return
    }

    const message = buildAssistantMessage(segment)
    messages.value.push(message)
    if (segment.kind === 'step') {
      activeStepMessageId.value = message.id
    } else {
      activeStepMessageId.value = ''
    }
  })

  return true
}

function findActiveAssistantDraft() {
  if (!activeAssistantId.value) {
    return null
  }
  return messages.value.find((item) => item.id === activeAssistantId.value) || null
}

async function scrollToBottom() {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

function closeCurrentStream() {
  if (streamController) {
    streamController.close()
    streamController = null
  }
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isSending.value || !props.canSend) {
    return
  }

  inputText.value = ''
  pushUserMessage(text)
  pendingThought.value = ''
  stepCounter.value = 0
  activeStepMessageId.value = ''
  isCapturingStepThought.value = false
  const assistantDraft = pushAssistantMessage()
  activeAssistantId.value = assistantDraft.id
  isSending.value = true
  scrollToBottom()

  closeCurrentStream()

  streamController = props.streamRequest({
    message: text,
    chatId: props.chatId,
    onMessage: async (chunk) => {
      if (isStepMode()) {
        appendAssistantSegments(chunk, assistantDraft)
      } else {
        appendPlainChunk(chunk, assistantDraft)
      }
      await scrollToBottom()
    },
    onDone: async () => {
      const draft = findActiveAssistantDraft() || assistantDraft
      isSending.value = false
      draft.loading = false
      if (isStepMode() && pendingThought.value.trim()) {
        const thoughtText = normalizeThoughtContent(pendingThought.value)
        const stepMessage = findMessageById(activeStepMessageId.value)
        if (stepMessage) {
          stepMessage.content = stepMessage.content ? `${stepMessage.content}\n${thoughtText}` : thoughtText
        } else if (!draft.content) {
          draft.content = thoughtText
        } else {
          messages.value.push(
            buildAssistantMessage({
              kind: 'reply',
              content: thoughtText
            })
          )
        }
        pendingThought.value = ''
      }

      if (!draft.content) {
        if (isStepMode()) {
          const draftIndex = messages.value.findIndex((item) => item.id === draft.id)
          if (draftIndex !== -1 && messages.value.length > 1) {
            messages.value.splice(draftIndex, 1)
          } else {
            draft.content = '已完成'
          }
        } else {
          draft.content = '已完成'
        }
      }

      activeAssistantId.value = ''
      activeStepMessageId.value = ''
      isCapturingStepThought.value = false
      streamController = null
      await scrollToBottom()
    },
    onError: async (errorText) => {
      const draft = findActiveAssistantDraft() || assistantDraft
      isSending.value = false
      draft.loading = false
      if (!draft.content) {
        draft.content = `请求失败：${errorText}`
      }
      activeAssistantId.value = ''
      activeStepMessageId.value = ''
      isCapturingStepThought.value = false
      streamController = null
      await scrollToBottom()
    }
  })
}

function onInputKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

onBeforeUnmount(() => {
  closeCurrentStream()
  activeAssistantId.value = ''
  activeStepMessageId.value = ''
  isCapturingStepThought.value = false
  pendingThought.value = ''
})
</script>

<template>
  <section class="chat-page">
    <header class="chat-header">
      <div>
        <p class="chat-title">{{ title }}</p>
        <p class="chat-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="chatId" class="chat-id-chip">
        会话 ID：{{ chatId }}
      </div>
    </header>

    <main ref="messageListRef" class="message-list">
      <article
        v-for="message in messages"
        :key="message.id"
        class="message-row"
        :class="message.role === 'user' ? 'message-row-user' : 'message-row-assistant'"
      >
        <div v-if="message.role === 'assistant' && assistantAvatar" class="assistant-avatar-wrap">
          <img class="assistant-avatar" :src="assistantAvatar" :alt="assistantAvatarAlt" />
        </div>
        <div class="message-bubble" :class="message.role === 'user' ? 'bubble-user' : 'bubble-assistant'">
          <p class="message-role">{{ message.role === 'user' ? '你' : 'AI' }}</p>
          <p class="message-content">{{ message.content || (message.loading ? '思考中...' : '') }}</p>
        </div>
      </article>

      <p v-if="messages.length === 0" class="empty-hint">
        开始输入你的第一条消息，AI 将实时回复。
      </p>
    </main>

    <footer class="composer">
      <textarea
        v-model="inputText"
        rows="3"
        :placeholder="placeholder"
        :disabled="isSending || !canSend"
        @keydown="onInputKeydown"
      />
      <button type="button" class="send-button" :disabled="isSending || !canSend" @click="sendMessage">
        {{ isSending ? '发送中...' : '发送' }}
      </button>
      <p v-if="blockedHint" class="blocked-hint">{{ blockedHint }}</p>
    </footer>
  </section>
</template>
