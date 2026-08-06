import type { UserRole } from '@/types/user'

// Everything role-related routes through this file on purpose. Right now
// `admin` and `maintainer` behave identically ("maintainer is admin++, but
// the ++ isn't built yet"). When that changes, this is the only file that
// needs to change — nothing else in the app should check `role === '...'`
// directly.

const ADMIN_LIKE: readonly UserRole[] = ['admin', 'maintainer']

export function isAdminLike(role: UserRole | null | undefined): boolean {
  return !!role && ADMIN_LIKE.includes(role)
}

export function canManageUsers(role: UserRole | null | undefined): boolean {
  return isAdminLike(role)
}

// Sensor CRUD isn't scoped per-user yet (the agreed sensor schema has no
// owner field this sprint), so every authenticated role can manage every
// sensor for now.
export function canManageSensors(role: UserRole | null | undefined): boolean {
  return !!role
}
