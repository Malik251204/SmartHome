import { api } from './api'
import type { Device, DeviceInput, DeviceStatus, DeviceType } from '@/types/device'

// CONFIRMED — real, working endpoints on backend/actual.
// Devices themselves come back nested inside RoomDto.devices (see
// roomService.ts). This service handles device writes: the frequent
// status toggle, plus full create/edit/remove for admin device
// management.
export interface RawDeviceDto {
  id: number
  name: string
  type: string | null
  unit: string | null
  status: string | null
  roomId: number | null
  roomName: string | null
}

export function toDevice(dto: RawDeviceDto): Device {
  return {
    id: String(dto.id),
    name: dto.name,
    type: (dto.type as DeviceType) ?? null,
    unit: dto.unit ?? '',
    status: (dto.status as DeviceStatus) ?? 'OFF',
    roomId: dto.roomId != null ? String(dto.roomId) : null,
    roomName: dto.roomName ?? null,
  }
}

// `unit` is a required column on the backend but isn't used/rendered
// anywhere in this UI — send a fixed placeholder rather than asking for
// it in the form.
function toBody(input: DeviceInput) {
  return {
    name: input.name,
    type: input.type,
    unit: 1,
    status: input.status,
    roomId: Number(input.roomId),
  }
}

export const deviceService = {
  async updateStatus(id: string, status: DeviceStatus): Promise<Device> {
    const { data } = await api.put<RawDeviceDto>(`/devices/${id}/status`, { status })
    return toDevice(data)
  },
  async create(input: DeviceInput): Promise<Device> {
    const { data } = await api.post<RawDeviceDto>('/devices', toBody(input))
    return toDevice(data)
  },
  // Full-overwrite semantics, matching the rest of this app.
  async update(id: string, input: DeviceInput): Promise<Device> {
    const { data } = await api.put<RawDeviceDto>(`/devices/${id}`, toBody(input))
    return toDevice(data)
  },
  async remove(id: string): Promise<void> {
    await api.delete(`/devices/${id}`)
  },
}
