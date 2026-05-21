<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { getApiErrorMessage, loginWithEmail, registerWithEmail } from '../api/auth'
import { isAuthenticated, setAuthState } from '../store/auth'

const router = useRouter()
const route = useRoute()

const mode = ref('login')
const submitting = ref(false)
const errorText = ref('')
const successText = ref('')
const form = reactive({
  email: '',
  password: ''
})

const title = computed(() => (mode.value === 'login' ? '欢迎登录' : '创建账号'))
const submitText = computed(() => {
  if (submitting.value) {
    return mode.value === 'login' ? '登录中...' : '注册中...'
  }
  return mode.value === 'login' ? '登录' : '注册'
})

const switchText = computed(() => (mode.value === 'login' ? '没有账号？去注册' : '已有账号？去登录'))

if (isAuthenticated()) {
  router.replace('/')
}

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  errorText.value = ''
  successText.value = ''
}

function validateForm() {
  if (!form.email.trim()) {
    return '请输入邮箱。'
  }
  if (!/^\S+@\S+\.\S+$/.test(form.email.trim())) {
    return '邮箱格式不正确。'
  }
  if (!form.password.trim()) {
    return '请输入密码。'
  }
  if (form.password.trim().length < 6) {
    return '密码长度至少为 6 位。'
  }
  return ''
}

async function handleSubmit() {
  const validationMessage = validateForm()
  if (validationMessage) {
    errorText.value = validationMessage
    successText.value = ''
    return
  }

  submitting.value = true
  errorText.value = ''
  successText.value = ''

  const payload = {
    email: form.email.trim(),
    password: form.password.trim()
  }

  try {
    if (mode.value === 'register') {
      const result = await registerWithEmail(payload)
      successText.value = result.message || '注册成功，请使用新账号登录。'
      mode.value = 'login'
      form.password = ''
      return
    }

    const loginResult = await loginWithEmail(payload)
    if (!loginResult.token) {
      errorText.value = '登录成功但未获取到 token，请检查后端返回。'
      return
    }

    setAuthState(loginResult)
    const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirectPath || '/')
  } catch (error) {
    errorText.value = getApiErrorMessage(
      error,
      mode.value === 'login' ? '登录失败，请稍后重试。' : '注册失败，请稍后重试。'
    )
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <nav class="top-nav">
      <RouterLink to="/" class="back-link">返回主页</RouterLink>
    </nav>

    <article class="auth-card">
      <p class="hero-kicker">YU AI ACCOUNT</p>
      <h1>{{ title }}</h1>
      <p class="auth-subtitle">登录后可访问需要认证的接口，并自动携带 Bearer Token。</p>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label class="auth-field">
          <span>邮箱</span>
          <input v-model="form.email" type="email" autocomplete="email" placeholder="user@example.com" />
        </label>

        <label class="auth-field">
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="至少 6 位"
          />
        </label>

        <button class="auth-submit" type="submit" :disabled="submitting">
          {{ submitText }}
        </button>
      </form>

      <p v-if="errorText" class="auth-message auth-error">{{ errorText }}</p>
      <p v-if="successText" class="auth-message auth-success">{{ successText }}</p>

      <button class="auth-switch" type="button" @click="toggleMode">{{ switchText }}</button>
    </article>
  </section>
</template>