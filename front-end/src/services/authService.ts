import { api } from './api'
import type { User } from '@/types/user'
import { roleFromBackend } from '@/utils/roleMapping'

export interface LoginPayload {
  email: string
  password: string
}

export interface SignupPayload {
  name: string
  email: string
  phoneNumber: string
  password: string
}

interface RawAuthResponse {
  token: string
  user: { id: number; name: string; email: string; phoneNumber: number; roles: string[] }
}

export interface AuthResult {
  token: string
  user: User
}

// CONFIRMED — real endpoints on backend/actual (permitAll, no token
// needed to call these two).
export const authService = {
  async login(payload: LoginPayload): Promise<AuthResult> {
    const { data } = await api.post<RawAuthResponse>('/auth/login', payload)
    return toResult(data)
  },

  async signup(payload: SignupPayload): Promise<AuthResult> {
    const { data } = await api.post<RawAuthResponse>('/auth/register', {
      name: payload.name,
      email: payload.email,
      password: payload.password,
      phoneNumber: Number(payload.phoneNumber) || 0,
    })
    return toResult(data)
  },
}

function toResult(data: RawAuthResponse): AuthResult {
  return {
    token: data.token,
    user: {
      id: String(data.user.id),
      name: data.user.name,
      email: data.user.email,
      phoneNumber: data.user.phoneNumber,
      role: roleFromBackend(data.user.roles),
    },
  }
}
