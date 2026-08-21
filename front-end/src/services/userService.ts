import { api } from './api'
import type { User, UserInput } from '@/types/user'
import { roleToBackend, roleFromBackend } from '@/utils/roleMapping'

// CONFIRMED — real, working endpoints on backend/actual.
export interface RawUserDto {
  id: number
  name: string
  email: string
  phoneNumber: number
  roles: string[]
}

export function toUser(dto: RawUserDto): User {
  return {
    id: String(dto.id),
    name: dto.name,
    email: dto.email,
    phoneNumber: dto.phoneNumber,
    role: roleFromBackend(dto.roles),
  }
}

function toBody(input: UserInput) {
  return {
    name: input.name,
    email: input.email,
    // Required by the backend on create; left blank on update means
    // "keep the existing password" (see UserServiceImpl.updateUser).
    password: input.password ?? '',
    phoneNumber: input.phoneNumber,
    roles: roleToBackend(input.role),
  }
}

export const userService = {
  async list(): Promise<User[]> {
    const { data } = await api.get<RawUserDto[]>('/users')
    return data.map(toUser)
  },
  async get(id: string): Promise<User> {
    const { data } = await api.get<RawUserDto>(`/users/${id}`)
    return toUser(data)
  },
  async create(input: UserInput): Promise<User> {
    const { data } = await api.post<RawUserDto>('/users', toBody(input))
    return toUser(data)
  },
  // Full-overwrite semantics, matching the rest of this app.
  async update(id: string, input: UserInput): Promise<User> {
    const { data } = await api.put<RawUserDto>(`/users/${id}`, toBody(input))
    return toUser(data)
  },
  async remove(id: string): Promise<void> {
    await api.delete(`/users/${id}`)
  },
}
