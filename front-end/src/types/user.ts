export const USER_ROLES = ['classic_user', 'maintainer', 'admin'] as const
export type UserRole = (typeof USER_ROLES)[number]

// CONFIRMED — matches backend/actual's real UserDto exactly.
// Room membership isn't part of this shape at all (User doesn't own that
// relationship — Room does). To find a user's rooms, cross-reference
// against the full room list (see roomService.ts) rather than expecting
// it here.
export interface User {
  id: string
  name: string
  email: string
  phoneNumber: number
  role: UserRole
}

export type UserInput = Omit<User, 'id'> & { password?: string }

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  classic_user: 'Classic user',
  maintainer: 'Maintainer',
  admin: 'Admin',
}
