import { api } from './api'
import type { Room, RoomDetail, RoomInput } from '@/types/room'
import { toSensor, type RawSensorDto } from './sensorService'
import { toUserFromRoomMembership, type NestedUserDto } from './userService'

const BASE_PATH = '/rooms'

// GET /api/rooms and GET /api/rooms/{id} both return this same full,
// nested RoomDto — no lightweight list variant on backend/actual.
//
// `devices` (generic actuators, ON/OFF/OPEN/CLOSED, no data/type) is not
// mapped into our Room types. Our curtains/bulb/AC actuators match the
// `sensors` shape instead (has type + JSON data). Worth confirming with
// your teammate — not a settled decision, just the best current read.
interface RawRoomDto {
  id: number
  name: string
  users: NestedUserDto[]
  devices: unknown[]
  sensors: RawSensorDto[]
}

function toRoom(dto: RawRoomDto): Room {
  return {
    id: String(dto.id),
    name: dto.name,
    sensorCount: dto.sensors.length,
    userCount: dto.users.length,
  }
}

function toRoomDetail(dto: RawRoomDto): RoomDetail {
  const ref = { id: String(dto.id), name: dto.name }
  return {
    id: String(dto.id),
    name: dto.name,
    sensors: dto.sensors.map(toSensor),
    users: dto.users.map((u) => toUserFromRoomMembership(u, ref)),
  }
}

// Room membership (sensors/users) is edited via sensorService/userService,
// not here — this service only manages the room's own name.
export const roomService = {
  async list(): Promise<Room[]> {
    const { data } = await api.get<RawRoomDto[]>(BASE_PATH)
    return data.map(toRoom)
  },

  async get(id: string): Promise<RoomDetail> {
    const { data } = await api.get<RawRoomDto>(`${BASE_PATH}/${id}`)
    return toRoomDetail(data)
  },

  // GUESSED — no write endpoints on backend/actual yet.
  async create(input: RoomInput): Promise<Room> {
    const { data } = await api.post<RawRoomDto>(BASE_PATH, input)
    return toRoom(data)
  },

  async update(id: string, input: RoomInput): Promise<Room> {
    const { data } = await api.put<RawRoomDto>(`${BASE_PATH}/${id}`, input)
    return toRoom(data)
  },

  async remove(id: string): Promise<void> {
    await api.delete(`${BASE_PATH}/${id}`)
  },
}
