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

// Login/signup themselves stay mocked (see project notes — the real backend
// has no auth at all yet, so there's nothing to swap to). But most other
// features (Preferences especially) now hit the real backend and need a
// real numeric user id, not our mock's fictional uuid. Best-effort fix:
// after a mock login/signup succeeds, try to resolve the same email to a
// real backend user and use THAT as the session's identity instead. If
// nothing matches (the common case, since our demo accounts don't exist in
// his database), we just keep the fictional identity — everything still
// works except features that need a real backend userId.
async function resolveRealIdentity(fallback: User): Promise<User> {
  try {
    const realUsers = await userService.list()
    const match = realUsers.find((u) => u.email.toLowerCase() === fallback.email.toLowerCase())
    return match ?? fallback
  } catch {
    return fallback
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => loadInitialState(),

  getters: {
    isAuthenticated: (state): boolean => !!state.token,
    role: (state) => state.user?.role ?? null,
    // True once `user` is a real backend record (not the fictional mock
    // identity) — features like Preferences that need a real userId can
    // check this to explain themselves instead of failing silently.
    hasRealIdentity: (state): boolean => !!state.user && /^\d+$/.test(state.user.id),
  },

  actions: {
    async login(email: string, password: string) {
      const { token, user } = await authService.login({ email, password })
      this.token = token
      this.user = await resolveRealIdentity(user)
      this.persist()
    },

    async signup(payload: SignupPayload) {
      // Create the REAL backend user first. If this fails, stop here —
      // don't touch the mock auth layer at all. The previous order created
      // the mock account first (unconditionally, always succeeds) and only
      // attempted the real one after, which meant a failed real creation
      // still left a permanently-loggable-into fake account behind with
      // nothing real backing it. Reordering fixes that at the root instead
      // of trying to clean up after the fact.
      const created = await userService.create({
        name: payload.name,
        email: payload.email,
        phoneNumber: Number(payload.phoneNumber) || 0,
        role: 'classic_user',
      })
      // Only now register the mock login credentials, using the same
      // payload — this is still needed since login itself stays mocked.
      const { token } = await authService.signup(payload)
      this.token = token
      this.user = created
      this.persist()
    },

    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem(STORAGE_KEY)
    },

    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ token: this.token, user: this.user }))
    },
  },
})
