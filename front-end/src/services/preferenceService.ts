import { api } from './api'
import type { PreferenceRule, PreferenceRuleInput } from '@/types/preference'

// FULLY GUESSED — backend/actual has no Preference entity/service/
// controller at all yet. Kept wired to a conventional route for when it
// does; nothing here is a confirmed contract.

interface RawPreferenceRuleDto {
  id: number
  userId: number
  deviceId: number
  deviceName: string
  condition: string
  action: string
  strict: boolean
  enabled: boolean
  createdAt: string
}

function toRule(dto: RawPreferenceRuleDto): PreferenceRule {
  return {
    id: String(dto.id),
    userId: String(dto.userId),
    deviceId: String(dto.deviceId),
    deviceName: dto.deviceName,
    condition: dto.condition,
    action: dto.action,
    strict: dto.strict,
    enabled: dto.enabled,
    createdAt: dto.createdAt,
  }
}

function toBody(input: PreferenceRuleInput) {
  return {
    userId: Number(input.userId),
    deviceId: Number(input.deviceId),
    deviceName: input.deviceName,
    condition: input.condition,
    action: input.action,
    strict: input.strict,
    enabled: input.enabled,
  }
}

export const preferenceService = {
  async list(userId: string): Promise<PreferenceRule[]> {
    const { data } = await api.get<RawPreferenceRuleDto[]>('/preferences', { params: { userId } })
    return data.map(toRule)
  },
  async create(input: PreferenceRuleInput): Promise<PreferenceRule> {
    const { data } = await api.post<RawPreferenceRuleDto>('/preferences', toBody(input))
    return toRule(data)
  },
  // Backend PUT overwrites every field — always send the full object.
  async update(id: string, input: PreferenceRuleInput): Promise<PreferenceRule> {
    const { data } = await api.put<RawPreferenceRuleDto>(`/preferences/${id}`, toBody(input))
    return toRule(data)
  },
  async remove(id: string): Promise<void> {
    await api.delete(`/preferences/${id}`)
  },
}
