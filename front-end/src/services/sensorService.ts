import { api } from './api'
import type {
  Sensor,
  SensorInput,
  SensorType,
  SensorStatus,
  SensorData,
  SensorReading,
  CurtainsData,
  LightBulbData,
  ACData,
} from '@/types/sensor'

// CONFIRMED: GET /api/sensors, GET /api/sensors/{id} on backend/actual.
// create/update/remove/readings are GUESSED — not implemented on
// backend/actual yet, kept wired for when they are; callers already
// handle the 404s.
const BASE_PATH = '/sensors'

export interface RawSensorDto {
  id: number
  name: string
  // backend/actual's `type` is a free-form String, not restricted to
  // CURTAINS/LIGHT_BULB/AC at the DB level. Kept as SensorType since
  // that's our product domain; parseSensorData() below degrades safely
  // on an unrecognized value.
  type: SensorType
  unit: string
  status: string
  data: string | null
  // roomId comes back as a String here (unlike DeviceDto.roomId, a Long
  // — inconsistency is backend/actual's own).
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

  if (type === 'CURTAINS') {
    return {
      isOpen: Boolean(parsed.isOpen ?? false),
      roomLightLux: typeof parsed.roomLightLux === 'number' ? (parsed.roomLightLux as number) : undefined,
    } satisfies CurtainsData
  }

  if (type === 'LIGHT_BULB') {
    return {
      isOn: Boolean(parsed.isOn ?? false),
      brightness: typeof parsed.brightness === 'number' ? (parsed.brightness as number) : 100,
    } satisfies LightBulbData
  }

  // Also the fallback for any unrecognized type string.
  return {
    mode: (parsed.mode as ACData['mode']) ?? 'OFF',
    targetTemp: typeof parsed.targetTemp === 'number' ? (parsed.targetTemp as number) : 22,
  } satisfies ACData
}

function serializeSensorData(data: SensorData): string {
  return JSON.stringify(data)
}

export function toSensor(dto: RawSensorDto): Sensor {
  return {
    id: String(dto.id),
    name: dto.name,
    type: dto.type,
    unit: dto.unit ?? '',
    // status is free-form on backend/actual too (e.g. "ACTIVE"/"INACTIVE",
    // not "on"/"off") — match case-insensitively, default to "on".
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

  async create(input: SensorInput): Promise<Sensor> {
    const body = {
      name: input.name,
      type: input.type,
      unit: input.unit,
      status: input.status,
      data: serializeSensorData(input.data),
      roomId: input.roomId,
    }
    const { data } = await api.post<RawSensorDto>(BASE_PATH, body)
    return toSensor(data)
  },

  // Full-overwrite semantics assumed, matching the rest of this app.
  async update(id: string, input: SensorInput): Promise<Sensor> {
    const body = {
      name: input.name,
      type: input.type,
      unit: input.unit,
      status: input.status,
      data: serializeSensorData(input.data),
      roomId: input.roomId,
    }
    const { data } = await api.put<RawSensorDto>(`${BASE_PATH}/${id}`, body)
    return toSensor(data)
  },

  async remove(id: string): Promise<void> {
    await api.delete(`${BASE_PATH}/${id}`)
  },

  // No /readings route on backend/actual — only backend/mock has one, and
  // it's not proxied yet (see SensorClient.java).
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
