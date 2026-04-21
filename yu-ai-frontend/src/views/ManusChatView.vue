<script setup>
import { RouterLink } from 'vue-router'
import ChatWindow from '../components/ChatWindow.vue'
import { openManusChatSse } from '../api/chat'
import manusAvatar from '../assets/manus-avatar.svg'
</script>

<template>
  <section>
    <nav class="top-nav">
      <RouterLink to="/" class="back-link">返回主页</RouterLink>
    </nav>

    <ChatWindow
      title="AI 超级智能体"
      subtitle="通过 doChatWithManus 接口进行流式对话。"
      placeholder="比如：请帮我拆解一个从 0 到 1 的产品上线计划。"
      response-mode="step-bubble"
      :assistant-avatar="manusAvatar"
      assistant-avatar-alt="超级智能体头像"
      :stream-request="openManusChatSse"
    />
  </section>
</template>
