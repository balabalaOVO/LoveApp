<script setup>
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { authState, clearAuthState, isAuthenticated } from '../store/auth'

const router = useRouter()

const loggedIn = computed(() => isAuthenticated())
const currentEmail = computed(() => authState.email || '已登录用户')

const apps = [
  {
    name: 'AI 恋爱大师',
    description: '在恋爱沟通、关系维护和表达技巧上，获得实时对话建议。',
    route: '/love-chat',
    badge: 'Love App'
  },
  {
    name: 'AI 超级智能体',
    description: '让 AI 超级智能体进行任务拆解、思路推演与互动问答。',
    route: '/manus-chat',
    badge: 'Manus Agent'
  }
]

function logout() {
  clearAuthState()
  router.push('/login')
}
</script>

<template>
  <section class="home-page">
    <header class="hero">
      <p class="hero-kicker">YU AI WORKSPACE</p>
      <h1>选择你的 AI 应用</h1>
      <p>一个入口，切换不同智能体场景。点击下方卡片进入对应聊天室。</p>

      <div class="home-auth-bar">
        <RouterLink v-if="!loggedIn" class="home-auth-link" to="/login">登录 / 注册</RouterLink>
        <div v-else class="home-auth-state">
          <span>当前账号：{{ currentEmail }}</span>
          <button type="button" class="home-logout-button" @click="logout">退出登录</button>
        </div>
      </div>
    </header>

    <div class="app-grid">
      <RouterLink v-for="item in apps" :key="item.route" :to="item.route" class="app-card">
        <p class="card-badge">{{ item.badge }}</p>
        <h2>{{ item.name }}</h2>
        <p>{{ item.description }}</p>
        <span class="card-action">进入应用</span>
      </RouterLink>
    </div>
  </section>
</template>
