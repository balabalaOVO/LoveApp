import { reactive } from 'vue'

const AUTH_STORAGE_KEY = 'yu_ai_auth'

function normalizeAuthState(payload = {}) {
  return {
    token: typeof payload.token === 'string' ? payload.token.trim() : '',
    userId: payload.userId ?? null,
    email: typeof payload.email === 'string' ? payload.email.trim() : ''
  }
}

function readAuthStateFromStorage() {
  if (typeof window === 'undefined') {
    return normalizeAuthState()
  }

  const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
  if (!raw) {
    return normalizeAuthState()
  }

  try {
    return normalizeAuthState(JSON.parse(raw))
  } catch {
    window.localStorage.removeItem(AUTH_STORAGE_KEY)
    return normalizeAuthState()
  }
}

function persistAuthState(state) {
  if (typeof window === 'undefined') {
    return
  }

  if (!state.token) {
    window.localStorage.removeItem(AUTH_STORAGE_KEY)
    return
  }

  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(state))
}

export const authState = reactive(readAuthStateFromStorage())

export function setAuthState(payload) {
  const nextState = normalizeAuthState(payload)
  authState.token = nextState.token
  authState.userId = nextState.userId
  authState.email = nextState.email
  persistAuthState(nextState)
}

export function clearAuthState() {
  authState.token = ''
  authState.userId = null
  authState.email = ''
  persistAuthState(authState)
}

export function getAuthToken() {
  return authState.token || ''
}

export function isAuthenticated() {
  return Boolean(authState.token)
}