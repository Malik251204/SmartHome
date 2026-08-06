import type { User } from '@/types/user'
import { seedUsers, DEMO_CREDENTIALS } from './data/users'

const USERS_KEY = 'homecontrol_mock_users'
const CREDENTIALS_KEY = 'homecontrol_mock_credentials'

function load<T>(key: string, seed: T): T {
  const raw = localStorage.getItem(key)
  if (!raw) return seed
  try {
    return JSON.parse(raw) as T
  } catch {
    return seed
  }
}

function persist(key: string, value: unknown) {
  localStorage.setItem(key, JSON.stringify(value))
}

// Auth is the only thing still mocked (see project notes — the real
// backend has no auth yet). Everything else — sensors, commands, readings,
// preferences — now goes straight to the real backend, so this "db" only
// needs to hold what mock login/signup needs: a fictional identity plus a
// mutable credentials table.
export const db = {
  users: load<User[]>(USERS_KEY, seedUsers),
  credentials: load<Record<string, string>>(CREDENTIALS_KEY, { ...DEMO_CREDENTIALS }),

  saveUsers() {
    persist(USERS_KEY, this.users)
  },
  saveCredentials() {
    persist(CREDENTIALS_KEY, this.credentials)
  },

  reset() {
    this.users = JSON.parse(JSON.stringify(seedUsers))
    this.credentials = { ...DEMO_CREDENTIALS }
    this.saveUsers()
    this.saveCredentials()
  },
}
