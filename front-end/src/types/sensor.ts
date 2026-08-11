// Sensor domain types. Sensors are read-only ambient room readings —
// independent of Devices (the controllable actuators, see room.ts /
// deviceService.ts). No create/edit/delete: sensors only ever come from
// seeding, via backend/mock.

export const SENSOR_TYPES = ['LUX', 'TEMPERATURE', 'OCCUPANCY'] as const
export type SensorType = (typeof SENSOR_TYPES)[number]

export type SensorStatus = 'on' | 'off'

export interface LuxData {
  lux: number
}

export interface TemperatureData {
  celsius: number
}

export interface OccupancyData {
  count: number
}

export type SensorData = LuxData | TemperatureData | OccupancyData

export interface Sensor {
  id: string
  name: string
  type: SensorType
  unit: string
  status: SensorStatus
  data: SensorData
  updatedAt: string
  roomId: string | null
  roomName: string | null
}

// A point-in-time snapshot from the backend's reading history (Reading
// entity) — real and persisted on backend/mock, though not yet proxied
// through backend/actual (see sensorService.ts).
export interface SensorReading {
  id: string
  sensorId: string
  timestamp: string
  data: SensorData
}

export const SENSOR_TYPE_LABELS: Record<SensorType, string> = {
  LUX: 'Light level',
  TEMPERATURE: 'Temperature',
  OCCUPANCY: 'Occupancy',
}

export const SENSOR_TYPE_UNITS: Record<SensorType, string> = {
  LUX: 'lux',
  TEMPERATURE: '\u00b0C',
  OCCUPANCY: 'people',
}

export function formatSensorValue(type: SensorType, data: SensorData): string {
  if (type === 'LUX') return `${(data as LuxData).lux} lux`
  if (type === 'TEMPERATURE') return `${(data as TemperatureData).celsius}\u00b0C`
  return `${(data as OccupancyData).count} ${(data as OccupancyData).count === 1 ? 'person' : 'people'}`
}
