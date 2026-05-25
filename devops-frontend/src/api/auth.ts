import { apiPost } from './client'
import type { LoginRequest, LoginVO, RegisterRequest, UserVO } from '@/types'

export const authApi = {
  login: (data: LoginRequest) => apiPost<LoginVO>('/auth/login', data),
  register: (data: RegisterRequest) => apiPost<UserVO>('/auth/register', data),
}
