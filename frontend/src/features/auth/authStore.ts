import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserResponse } from '../../api/generated'

type AuthState = {
  token: string | null
  user: UserResponse | null
  setAuth: (token: string, user: UserResponse) => void
  logout: () => void
}

/**
 * Token lives in localStorage via persist — the pragmatic learning-app choice.
 * Enterprise caveat (documented in the README): localStorage is readable by
 * any XSS payload; real products keep access tokens in memory with an
 * HttpOnly refresh cookie, or delegate to an IdP.
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      setAuth: (token, user) => set({ token, user }),
      logout: () => set({ token: null, user: null }),
    }),
    { name: 'restaurant-auth', version: 1 },
  ),
)
