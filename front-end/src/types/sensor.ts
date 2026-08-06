// Sensor domain types.


export const SENSOR_TYPES = ['CURTAINS', 'LIGHT_BULB', 'AC'] as const
export type SensorType = (typeof SENSOR_TYPES)[number]

export type SensorStatus = 'on' | 'off'


export interface CurtainsData {
  isOpen: boolean
  roomLightLux?: number
}


export interface LightBulbData {
  isOn: boolean
  brightness: number // 0-100
}

export type ACMode = 'OFF' | 'HEAT' | 'COOL'


export interface ACData {
  mode: ACMode
  targetTemp: number // degrees C
}

export type SensorData = CurtainsData | LightBulbData | ACData

export interface Sensor {
  id: string
  name: string
  type: SensorType
  unit: string
  status: SensorStatus
  data: SensorData
  updatedAt: string
  // Real relation on the backend now (a Room table exists). null = unassigned.
  roomId: string | null
  roomName: string | null
}

// roomName is server-computed (derived from the room relation) — not part
// of what a caller writes, only what they read back.
export type SensorInput = Omit<Sensor, 'id' | 'updatedAt' | 'roomName'>

// A point-in-time snapshot from the backend's reading history (Reading
// entity) — real and persisted now, not a client-side approximation.
export interface SensorReading {
  id: string
  sensorId: string
  timestamp: string
  data: SensorData
}

export function defaultDataFor(type: SensorType): SensorData {
  switch (type) {
    case 'CURTAINS':
      return { isOpen: false }
    case 'LIGHT_BULB':
      return { isOn: false, brightness: 100 }
    case 'AC':
      return { mode: 'OFF', targetTemp: 22 }
  }
}

// `unit` on backend/actual is actually a stringified Double (e.g. "1.0"),
// not a descriptive string like "%". Intended meaning unconfirmed.
export function defaultUnitFor(type: SensorType): string {
  switch (type) {
    case 'CURTAINS':
      return ''
    case 'LIGHT_BULB':
      return '%'
    case 'AC':
      return '\u00b0C'
  }
}

export const SENSOR_TYPE_LABELS: Record<SensorType, string> = {
  CURTAINS: 'Curtains',
  LIGHT_BULB: 'Light bulb',
  AC: 'Air conditioner',
}
