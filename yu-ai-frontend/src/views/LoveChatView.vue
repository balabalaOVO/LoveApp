<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import ChatWindow from '../components/ChatWindow.vue'
import ConversationSidebar from '../components/ConversationSidebar.vue'
import { openLoveChatSse, listConversations, getConversationMessages, deleteConversation } from '../api/chat'
import loveMasterAvatar from '../assets/love-master-avatar-user.png'

const conversations = ref([])
const chatId = ref('')
const initialMessages = ref([])
const loadingHistory = ref(false)
const loadingList = ref(false)

const subtitle = computed(() => {
  if (loadingHistory.value) return '正在加载历史对话...'
  if (!chatId.value) return '点击"新建对话"开始，或选择左侧历史对话。'
  return '对话已自动保存，可随时在左侧切换历史记录。'
})

function generateChatId() {
  const time = Date.now().toString(36)
  const random = Math.random().toString(36).slice(2, 8)
  return `love-${time}-${random}`
}

async function loadConversations() {
  loadingList.value = true
  try {
    conversations.value = await listConversations('love_app')
  } catch (e) {
    console.error('Failed to load conversations', e)
    conversations.value = []
  } finally {
    loadingList.value = false
  }
}

async function selectConversation(conv) {
  loadingHistory.value = true
  chatId.value = conv.chatKey
  try {
    const msgs = await getConversationMessages(conv.chatKey)
    initialMessages.value = msgs
  } catch (e) {
    console.error('Failed to load messages', e)
    initialMessages.value = []
  } finally {
    loadingHistory.value = false
  }
}

async function handleNewChat() {
  chatId.value = generateChatId()
  initialMessages.value = []
}

async function handleDelete(conv) {
  if (!confirm(`确定删除"${conv.title || '未命名对话'}"？`)) return
  try {
    await deleteConversation(conv.chatKey)
    conversations.value = conversations.value.filter(c => c.chatKey !== conv.chatKey)
    if (chatId.value === conv.chatKey) {
      chatId.value = generateChatId()
      initialMessages.value = []
    }
  } catch (e) {
    console.error('Failed to delete conversation', e)
  }
}

async function onFirstMessage() {
  await loadConversations()
}

onMounted(async () => {
  await loadConversations()
  if (conversations.value.length > 0) {
    await selectConversation(conversations.value[0])
  } else {
    chatId.value = generateChatId()
  }
})
</script>

<template>
  <section class="chat-layout">
    <ConversationSidebar
      app-type="love_app"
      :active-chat-id="chatId"
      :conversations="conversations"
      :loading="loadingList"
      @select="selectConversation"
      @delete="handleDelete"
      @new-chat="handleNewChat"
    />
    <main class="chat-main">
      <nav class="top-nav">
        <RouterLink to="/" class="back-link">返回主页</RouterLink>
      </nav>
      <ChatWindow
        title="AI 恋爱大师"
        :subtitle="subtitle"
        :chat-id="chatId"
        :initial-messages="initialMessages"
        placeholder="比如：今天和对方冷场了，我该怎么自然破冰？"
        response-mode="single-bubble"
        :assistant-avatar="loveMasterAvatar"
        assistant-avatar-alt="恋爱大师头像"
        :stream-request="openLoveChatSse"
        @first-message="onFirstMessage"
      />
    </main>
  </section>
</template>
