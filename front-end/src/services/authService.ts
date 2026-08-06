import { api } from './api'
import type { User } from '@/types/user'

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

export interface LoginResponse {
  token: string
  user: User
}

// Everything here is what the real backend needs to replicate later:
// POST /auth/login with { email, password }, returning { token, user }.
// When Spring Security lands, only the response shape needs revisiting —
// the auth store and route guards don't know or care that this is a mock.
export const authService = {
  login(payload: LoginPayload): Promise<LoginResponse> {
    return api.post<LoginResponse>('/auth/login', payload).then((r) => r.data)
  },

  // Always creates a classic_user account server-side (see the mock handler);
  // admin/maintainer accounts are provisioned via userService instead.
  signup(payload: SignupPayload): Promise<LoginResponse> {
    return api.post<LoginResponse>('/auth/signup', payload).then((r) => r.data)
  },
}
