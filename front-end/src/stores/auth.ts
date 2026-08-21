import { defineStore } from 'pinia'
import { authService, type SignupPayload } from '@/services/authService'
import { userService } from '@/services/userService'
import type { User } from '@/types/user'

const STORAGE_KEY = 'homecontrol_auth'

interface AuthState {
  user: User | null
  token: string | null
}

function loadInitialState(): AuthState {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return { user: null, token: null }
  try {
    return JSON.parse(raw) as AuthState
  } catch {
    return { user: null, token: null }
  }
}

// Auth is real now — login/register hit backend/actual directly and
// return a real user with a real numeric id, so there's nothing to
// reconcile here (no fictional identity, no fallback).
export const useAuthStore = defineStore('auth', {
  state: (): AuthState => loadInitialState(),

  getters: {
    isAuthenticated: (state): boolean => !!state.token,
    role: (state) => state.user?.role ?? null,
  },

  actions: {
    async login(email: string, password: string) {
      const { token, user } = await authService.login({ email, password })
      this.token = token
      this.user = user
      this.persist()
    },

    async signup(payload: SignupPayload) {
      const { token, user } = await authService.signup(payload)
      this.token = token
      this.user = user
      this.persist()
    },

    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem(STORAGE_KEY)
    },

    // Self-service edit from the Account page. Full-overwrite semantics
    // like every other update in this app — role and password are left
    // as-is (role carried over from the current user; password omitted,
    // which userService/backend both treat as "keep the existing one").
    async updateProfile(input: { name: string; email: string; phoneNumber: string }) {
      if (!this.user) throw new Error('Not logged in')
      const updated = await userService.update(this.user.id, {
        name: input.name,
        email: input.email,
        phoneNumber: Number(input.phoneNumber),
        role: this.user.role,
      })
      this.user = updated
      this.persist()
    },

    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ token: this.token, user: this.user }))
    },
  },
})
