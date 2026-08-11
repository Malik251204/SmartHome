// A preference is a free-form statement from a user, optionally scoped to
// one room. Unscoped (roomId null) means "applies to every room this user
// is currently assigned to" — resolved dynamically, not stored as a list.
export interface PreferenceRule {
  id: string
  userId: string
  roomId: string | null
  roomName: string | null
  text: string
  enabled: boolean
  createdAt: string
}

export type PreferenceRuleInput = { userId: string; roomId: string | null; text: string; enabled: boolean }
