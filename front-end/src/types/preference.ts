// Matches the class diagram's PreferenceRule fields, plus a denormalized
// deviceName for display (same rationale as Command.sensorName).
export interface PreferenceRule {
  id: string
  userId: string
  deviceId: string
  deviceName: string
  condition: string
  action: string
  strict: boolean
  enabled: boolean
  createdAt: string
}

export type PreferenceRuleInput = Omit<PreferenceRule, 'id' | 'createdAt'>
