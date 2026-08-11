import { v4 as uuid } from 'uuid'
import type { User } from '@/types/user'

export const seedUsers: User[] = [
  {
    id: uuid(),
    name: 'Amine Admin',
    email: 'admin@homecontrol.io',
    phoneNumber: 21620000001,
    role: 'admin',
  },
  {
    id: uuid(),
    name: 'Mia Maintainer',
    email: 'maintainer@homecontrol.io',
    phoneNumber: 21620000002,
    role: 'maintainer',
  },
  {
    id: uuid(),
    name: 'Cyrine Classic',
    email: 'user@homecontrol.io',
    phoneNumber: 21620000003,
    role: 'classic_user',
  },
  // Matches DataSeeder's real seeded admin (alice@example.com, ADMIN) on
  // backend/actual. Logging in with this email/password succeeds at the
  // mock layer, then resolveRealIdentity() (stores/auth.ts) swaps in the
  // real backend record — real numeric id, real ADMIN role — so this is a
  // one-step way to test as a real admin, no signup+promote+relogin dance.
  // The name/role here are just the fallback if the real lookup ever
  // fails; what you actually get on a successful login is Alice's real
  // record.
  {
    id: uuid(),
    name: 'Alice Smith',
    email: 'alice@example.com',
    phoneNumber: 555123456,
    role: 'admin',
  },
]

// Mock-only login credentials. This map is deliberately separate from the
// User CRUD records above (a real backend will keep credentials in its own
// auth/security layer, not on the user profile) — see services/authService.ts.
export const DEMO_CREDENTIALS: Record<string, string> = {
  'admin@homecontrol.io': 'admin123',
  'maintainer@homecontrol.io': 'maintainer123',
  'user@homecontrol.io': 'user123',
  'alice@example.com': 'alice123',
}
