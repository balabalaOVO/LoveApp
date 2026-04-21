<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import ChatWindow from '../components/ChatWindow.vue'
import { openLoveChatSse } from '../api/chat'
import loveMasterAvatar from '../assets/love-master-avatar-user.png'

const chatId = ref(generateChatId())

const subtitle = computed(() => '进入页面后已自动创建会话，可持续在同一个 chatId 下聊天。')

function generateChatId() {
  const time = Date.now().toString(36)
  const random = Math.random().toString(36).slice(2, 8)
  return `love-${time}-${random}`
}
</script>

<template>
  <section>
    <nav class="top-nav">
      <RouterLink to="/" class="back-link">返回主页</RouterLink>
    </nav>

    <ChatWindow
      title="AI 恋爱大师"
      :subtitle="subtitle"
      :chat-id="chatId"
      placeholder="比如：今天和对方冷场了，我该怎么自然破冰？"
      response-mode="single-bubble"
      :assistant-avatar="loveMasterAvatar"
      assistant-avatar-alt="恋爱大师头像"
      :stream-request="openLoveChatSse"
    />
  </section>
</template>
