import axios from 'axios'
import { getAuthToken } from '../store/auth'

function resolveApiBaseUrl() {
  const envBase = import.meta.env.VITE_API_BASE_URL
  if (envBase) {
    return /^https?:\/\//i.test(envBase) ? envBase : new URL(envBase, window.location.origin).toString()
  }
  return `${window.location.origin}/api`
}

export const apiClient = axios.create({
  baseURL: resolveApiBaseUrl(),
  timeout: 30000
})

apiClient.interceptors.request.use((config) => {
  const token = getAuthToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function buildApiUrl(path, params = {}) {
  const base = apiClient.defaults.baseURL || ''
  const normalizedBase = base.endsWith('/') ? base : `${base}/`
  const normalizedPath = /^https?:\/\//i.test(path) ? path : path.replace(/^\//, '')
  const url = new URL(normalizedPath, normalizedBase)
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  return url.toString()
}
