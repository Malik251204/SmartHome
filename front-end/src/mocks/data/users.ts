import { v4 as uuid } from 'uuid'
import type { User } from '@/types/user'

export const seedUsers: User[] = [
  {
    id: uuid(),
    name: 'Amine Admin',
    email: 'admin@homecontrol.io',
    phoneNumber: '+216 20 000 001',
    role: 'admin',
    roomIds: [],
    rooms: [],
    createdAt: new Date().toISOString(),
  },
  {
    id: uuid(),
    name: 'Mia Maintainer',
    email: 'maintainer@homecontrol.io',
    phoneNumber: '+216 20 000 002',
    role: 'maintainer',
    roomIds: [],
    rooms: [],
    createdAt: new Date().toISOString(),
  },
  {
    id: uuid(),
    name: 'Cyrine Classic',
    email: 'user@homecontrol.io',
    phoneNumber: '+216 20 000 003',
    role: 'classic_user',
    // This mock identity is fictional and never has a real numeric backend
    // id (see stores/auth.ts's hasRealIdentity), so it can't reference a
    // real Room row either — it stays unassigned until resolveRealIdentity
    // swaps in an actual backend user.
    roomIds: [],
    rooms: [],
    createdAt: new Date().toISOString(),
  },
  {
    id: uuid(),
    name: 'Alice Smith',
    email: 'alice@example.com',
    phoneNumber: '+216 20 000 004',
    role: 'admin',
    roomIds: [],
    rooms: [],
    createdAt: new Date().toISOString(),
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
