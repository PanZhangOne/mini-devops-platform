import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { LoginVO } from '@/types'

interface AuthState {
  token: string | null
  userId: number | null
  username: string | null
  nickname: string | null
  isAuthenticated: boolean
  login: (data: LoginVO) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userId: null,
      username: null,
      nickname: null,
      isAuthenticated: false,
      login: (data) =>
        set({
          token: data.token,
          userId: data.userId,
          username: data.username,
          nickname: data.nickname,
          isAuthenticated: true,
        }),
      logout: () =>
        set({
          token: null,
          userId: null,
          username: null,
          nickname: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: 'auth-storage',
    },
  ),
)
