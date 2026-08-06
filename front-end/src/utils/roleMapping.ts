import type { UserRole } from '@/types/user'

// backend/actual's Role enum (model/enums/Role.java) is only
// {ADMIN, USER} — no CREATOR/maintainer equivalent. Lossy best-effort:
// maintainer -> ADMIN, until raised with your teammate.
const TO_BACKEND: Record<UserRole, string> = {
  admin: 'ADMIN',
  maintainer: 'ADMIN',
  classic_user: 'USER',
}

const FROM_BACKEND: Record<string, UserRole> = {
  ADMIN: 'admin',
  USER: 'classic_user',
}

export function roleToBackend(role: UserRole): string[] {
  return [TO_BACKEND[role]]
}

// A user could in theory hold multiple backend roles; we just take the
// highest-privilege one we recognize so the UI always has exactly one.
const PRIORITY: UserRole[] = ['admin', 'maintainer', 'classic_user']

export function roleFromBackend(roles: string[]): UserRole {
  const mapped = roles.map((r) => FROM_BACKEND[r]).filter((r): r is UserRole => !!r)
  for (const candidate of PRIORITY) {
    if (mapped.includes(candidate)) return candidate
  }
  return 'classic_user'
}
