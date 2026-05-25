import axios from 'axios'
import type { Result } from '@/types'

const client = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT token to every request
client.interceptors.request.use((config) => {
  const raw = localStorage.getItem('auth-storage')
  if (raw) {
    try {
      const parsed = JSON.parse(raw) as { state?: { token?: string } }
      const token = parsed?.state?.token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch {
      // ignore malformed storage
    }
  }
  return config
})

// Handle 401 → redirect to login; business code !== 0 → reject with message
client.interceptors.response.use(
  (res) => {
    const body = res.data as Result<unknown>
    if (body && typeof body.code === 'number' && body.code !== 0) {
      const err = new Error(body.message ?? '请求失败')
      ;(err as Error & { bizCode: number }).bizCode = body.code
      return Promise.reject(err)
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth-storage')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export async function apiGet<T>(url: string, params?: object): Promise<T> {
  const res = await client.get<Result<T>>(url, { params })
  return res.data.data
}

export async function apiPost<T>(url: string, data?: unknown): Promise<T> {
  const res = await client.post<Result<T>>(url, data)
  return res.data.data
}

export async function apiPut<T>(url: string, data?: unknown): Promise<T> {
  const res = await client.put<Result<T>>(url, data)
  return res.data.data
}

export async function apiPatch<T>(url: string, data?: unknown): Promise<T> {
  const res = await client.patch<Result<T>>(url, data)
  return res.data.data
}

export async function apiDelete<T = void>(url: string): Promise<T> {
  const res = await client.delete<Result<T>>(url)
  return res.data.data
}

export default client
