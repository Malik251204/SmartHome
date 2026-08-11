import { api } from './api'
import type { Sensor, SensorType, SensorStatus, SensorData, SensorReading, LuxData, TemperatureData, OccupancyData } from '@/types/sensor'

// CONFIRMED: GET /api/sensors, GET /api/sensors/{id} on backend/actual —
// proxied live from backend/mock. No write methods: sensors are read-only,
// seeded once via DataSeeder, never created/edited through the app.
const BASE_PATH = '/sensors'

export interface RawSensorDto {
  id: number
  name: string
  type: SensorType
  unit: string
  status: string
  data: string | null
  // Comes back as a String here (unlike DeviceDto.roomId, a Long —
  // inconsistency is backend/actual's own).
  roomId: string | null
  roomName: string | null
}

interface RawReadingDto {
  id: number
  sensorId: number
  recordedAt: string
  data: string | null
}

function parseSensorData(type: SensorType, raw: string | null): SensorData {
  let parsed: Record<string, unknown> = {}
  if (raw) {
    try {
      parsed = JSON.parse(raw)
    } catch {
      parsed = {}
    }
  }

  if (type === 'LUX') {
    return { lux: typeof parsed.lux === 'number' ? (parsed.lux as number) : 0 } satisfies LuxData
  }
  if (type === 'TEMPERATURE') {
    return { celsius: typeof parsed.celsius === 'number' ? (parsed.celsius as number) : 0 } satisfies TemperatureData
  }
  // Also the fallback for any unrecognized type string.
  return { count: typeof parsed.count === 'number' ? (parsed.count as number) : 0 } satisfies OccupancyData
}

export function toSensor(dto: RawSensorDto): Sensor {
  return {
    id: String(dto.id),
    name: dto.name,
    type: dto.type,
    unit: dto.unit ?? '',
    // status is free-form on backend/actual (e.g. "ACTIVE"/"INACTIVE", not
    // "on"/"off") — match case-insensitively, default to "on".
    status: (['off', 'inactive'].includes((dto.status ?? '').toLowerCase()) ? 'off' : 'on') as SensorStatus,
    data: parseSensorData(dto.type, dto.data),
    roomId: dto.roomId ?? null,
    roomName: dto.roomName ?? null,
    updatedAt: new Date().toISOString(),
  }
}

export const sensorService = {
  async list(): Promise<Sensor[]> {
    const { data } = await api.get<RawSensorDto[]>(BASE_PATH)
    return data.map(toSensor)
  },

  // GUESSED — no /readings route on backend/actual, only backend/mock has
  // one, and it's not proxied yet (see SensorClient.java).
  async readings(id: string, type: SensorType, limit = 20): Promise<SensorReading[]> {
    const { data } = await api.get<RawReadingDto[]>(`${BASE_PATH}/${id}/readings`, { params: { limit } })
    return data
      .slice()
      .reverse() // backend returns newest-first; sparkline wants oldest-first
      .map((r) => ({
        id: String(r.id),
        sensorId: String(r.sensorId),
        timestamp: r.recordedAt,
        data: parseSensorData(type, r.data),
      }))
  },
}
