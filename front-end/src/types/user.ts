import type { RoomRef } from './room'

export const USER_ROLES = ['classic_user', 'maintainer', 'admin'] as const
export type UserRole = (typeof USER_ROLES)[number]

// backend/actual has no /api/users yet — only RoomDto's nested users
// ({id, name, email}) are real. phoneNumber/role/rooms below are UI
// placeholders until a real user endpoint exists — see userService.ts.
export interface User {
  id: string
  name: string
  email: string
  phoneNumber: string
  role: UserRole
  createdAt: string
  // Real relation on the backend now — a user can belong to any number of
  // rooms at once (e.g. staff overseeing several).
  roomIds: string[]
  rooms: RoomRef[]
}

// `rooms` is server-computed (derived from roomIds) — not part of what a
// caller writes, only what they read back.
export type UserInput = Omit<User, 'id' | 'createdAt' | 'rooms'>

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  classic_user: 'Classic user',
  maintainer: 'Maintainer',
  admin: 'Admin',
}
