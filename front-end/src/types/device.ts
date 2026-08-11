// Device domain types. Devices are the controllable actuators (AC, light
// bulb, curtains) — distinct from Sensors (read-only ambient readings, see
// sensor.ts). Devices live entirely in backend/actual; no relation to
// Sensors beyond happening to sit in the same room.

export const DEVICE_TYPES = ['AC', 'LIGHT_BULB', 'CURTAINS'] as const
export type DeviceType = (typeof DEVICE_TYPES)[number]

// Each type has its own two-state status vocabulary.
export type DeviceStatus = 'ON' | 'OFF' | 'OPEN' | 'CLOSED'

export const DEVICE_TYPE_LABELS: Record<DeviceType, string> = {
  AC: 'Air conditioner',
  LIGHT_BULB: 'Light bulb',
  CURTAINS: 'Curtains',
}

// AC/LIGHT_BULB toggle ON/OFF; CURTAINS toggles OPEN/CLOSED.
export const DEVICE_STATUS_PAIR: Record<DeviceType, [DeviceStatus, DeviceStatus]> = {
  AC: ['OFF', 'ON'],
  LIGHT_BULB: ['OFF', 'ON'],
  CURTAINS: ['CLOSED', 'OPEN'],
}

export interface Device {
  id: string
  name: string
  type: DeviceType | null
  unit: string
  status: DeviceStatus
  roomId: string | null
  roomName: string | null
}
