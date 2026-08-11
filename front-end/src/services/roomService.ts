import { api } from './api'
import type { Room, RoomDetail, RoomInput } from '@/types/room'
import { toSensor, type RawSensorDto } from './sensorService'
import { toDevice, type RawDeviceDto } from './deviceService'
import { toUser, type RawUserDto } from './userService'

const BASE_PATH = '/rooms'

// GET /api/rooms and GET /api/rooms/{id} both return this same full,
// nested RoomDto — no lightweight list variant on backend/actual.
// `users` here is the same full UserDto shape as GET /api/users, so
// there's no separate "nested user" mapping needed anymore.
interface RawRoomDto {
  id: number
  name: string
  users: RawUserDto[]
  devices: RawDeviceDto[]
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
  return {
    id: String(dto.id),
    name: dto.name,
    sensors: dto.sensors.map(toSensor),
    devices: dto.devices.map(toDevice),
    users: dto.users.map(toUser),
  }
}

// CONFIRMED — all real, working endpoints on backend/actual.
export const roomService = {
  async list(): Promise<Room[]> {
    const { data } = await api.get<RawRoomDto[]>(BASE_PATH)
    return data.map(toRoom)
  },

  // Full detail for every room, not just the summary — used by the User
  // detail page to work out which rooms a user belongs to, since User
  // doesn't own that relationship and has no reverse lookup of its own.
  async listDetailed(): Promise<RoomDetail[]> {
    const { data } = await api.get<RawRoomDto[]>(BASE_PATH)
    return data.map(toRoomDetail)
  },

  async get(id: string): Promise<RoomDetail> {
    const { data } = await api.get<RawRoomDto>(`${BASE_PATH}/${id}`)
    return toRoomDetail(data)
  },

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

  // Room owns the user relationship — assignment lives here, not on
  // userService.
  async assignUser(roomId: string, userId: string): Promise<RoomDetail> {
    const { data } = await api.post<RawRoomDto>(`${BASE_PATH}/${roomId}/users/${userId}`)
    return toRoomDetail(data)
  },

  async removeUser(roomId: string, userId: string): Promise<RoomDetail> {
    const { data } = await api.delete<RawRoomDto>(`${BASE_PATH}/${roomId}/users/${userId}`)
    return toRoomDetail(data)
  },
}
