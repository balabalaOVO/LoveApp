import axios from 'axios'

export const apiClient = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 30000
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
