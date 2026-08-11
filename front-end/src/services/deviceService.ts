import { api } from './api'
import type { Device, DeviceStatus, DeviceType } from '@/types/device'

// CONFIRMED — real, working endpoint on backend/actual.
// Devices themselves come back nested inside RoomDto.devices (see
// roomService.ts); this service only handles the one write path that
// exists: flipping a device's status.
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

export const deviceService = {
  async updateStatus(id: string, status: DeviceStatus): Promise<Device> {
    const { data } = await api.put<RawDeviceDto>(`/devices/${id}/status`, { status })
    return toDevice(data)
  },
}
