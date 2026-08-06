import { api } from './api'
import type { User, UserInput } from '@/types/user'
import type { RoomRef } from '@/types/room'
import { roleToBackend, roleFromBackend } from '@/utils/roleMapping'

// GUESSED — backend/actual has no /api/users at all yet, only a
// UserRepository with no controller. Every method here is speculative.
// The only CONFIRMED user shape comes from RoomDto's nested users
// ({id, name, email} only) — see toUserFromRoomMembership() below,
// used by roomService.

export interface RawUserDto {
  id: number
  name: string
  email: string
  phoneNumber: string
  roomIds: number[] | null
  rooms: { id: number; name: string }[] | null
  roles: string[]
}

export function toUser(dto: RawUserDto): User {
  const rooms: RoomRef[] = (dto.rooms ?? []).map((r) => ({ id: String(r.id), name: r.name }))
  return {
    id: String(dto.id),
    name: dto.name,
    email: dto.email,
    phoneNumber: dto.phoneNumber,
    role: roleFromBackend(dto.roles),
    roomIds: rooms.map((r) => r.id),
    rooms,
    createdAt: new Date().toISOString(),
  }
}

// Real shape of RoomDto.users: just {id, name, email}. Role and other
// room memberships aren't known from this endpoint.
export interface NestedUserDto {
  id: number
  name: string
  email: string
}

export function toUserFromRoomMembership(dto: NestedUserDto, room: RoomRef): User {
  return {
    id: String(dto.id),
    name: dto.name,
    email: dto.email,
    phoneNumber: '', // not returned by this endpoint
    role: 'classic_user', // not returned either; safest default, not a guess
    roomIds: [room.id],
    rooms: [room],
    createdAt: new Date().toISOString(),
  }
}

function toBody(input: UserInput) {
  return {
    name: input.name,
    email: input.email,
    phoneNumber: input.phoneNumber,
    roomIds: input.roomIds.map((id) => Number(id)),
    roles: roleToBackend(input.role),
  }
}

export const userService = {
  async list(): Promise<User[]> {
    const { data } = await api.get<RawUserDto[]>('/users')
    return data.map(toUser)
  },
  async create(input: UserInput): Promise<User> {
    const { data } = await api.post<RawUserDto>('/users', toBody(input))
    return toUser(data)
  },
  // Assumed full-overwrite semantics — always send the full object,
  // including the complete desired room membership, not a diff.
  async update(id: string, input: UserInput): Promise<User> {
    const { data } = await api.put<RawUserDto>(`/users/${id}`, toBody(input))
    return toUser(data)
  },
  async remove(id: string): Promise<void> {
    await api.delete(`/users/${id}`)
  },
}
