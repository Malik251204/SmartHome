import { api } from './api'
import type { PreferenceRule, PreferenceRuleInput } from '@/types/preference'

// CONFIRMED — real, working endpoints on backend/actual.
interface RawPreferenceDto {
  id: number
  userId: number
  roomId: number | null
  roomName: string | null
  text: string
  enabled: boolean
  createdAt: string
}

function toPreference(dto: RawPreferenceDto): PreferenceRule {
  return {
    id: String(dto.id),
    userId: String(dto.userId),
    roomId: dto.roomId != null ? String(dto.roomId) : null,
    roomName: dto.roomName,
    text: dto.text,
    enabled: dto.enabled,
    createdAt: dto.createdAt,
  }
}

function toBody(input: PreferenceRuleInput) {
  return {
    userId: Number(input.userId),
    roomId: input.roomId != null ? Number(input.roomId) : null,
    text: input.text,
    enabled: input.enabled,
  }
}

export const preferenceService = {
  async listForUser(userId: string): Promise<PreferenceRule[]> {
    const { data } = await api.get<RawPreferenceDto[]>('/preferences', { params: { userId } })
    return data.map(toPreference)
  },
  async create(input: PreferenceRuleInput): Promise<PreferenceRule> {
    const { data } = await api.post<RawPreferenceDto>('/preferences', toBody(input))
    return toPreference(data)
  },
  // Full-overwrite semantics, matching the rest of this app.
  async update(id: string, input: PreferenceRuleInput): Promise<PreferenceRule> {
    const { data } = await api.put<RawPreferenceDto>(`/preferences/${id}`, toBody(input))
    return toPreference(data)
  },
  async remove(id: string): Promise<void> {
    await api.delete(`/preferences/${id}`)
  },
}
