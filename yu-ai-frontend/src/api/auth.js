import { apiClient } from './http'

export async function registerWithEmail({ email, password }) {
  const response = await apiClient.post('/auth/register', {
    email,
    password
  })

  if (typeof response.data === 'string') {
    return {
      message: response.data
    }
  }

  return {
    message: response.data?.message || '注册成功'
  }
}

export async function loginWithEmail({ email, password }) {
  const response = await apiClient.post('/auth/login', {
    email,
    password
  })

  return {
    token: response.data?.token || '',
    userId: response.data?.userId ?? null,
    email: response.data?.email || email
  }
}

export function getApiErrorMessage(error, fallbackMessage = '请求失败，请稍后重试。') {
  const responseData = error?.response?.data

  if (typeof responseData === 'string' && responseData.trim()) {
    return responseData
  }

  if (responseData?.error) {
    return responseData.error
  }

  if (responseData?.message) {
    return responseData.message
  }

  return fallbackMessage
}