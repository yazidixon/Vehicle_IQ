import { api } from './api'
import type { LoginPayload, RegisterPayload, TokenResponse, User } from '../types/auth'

export const authService = {
  async register(payload: RegisterPayload): Promise<TokenResponse> {
    const { data } = await api.post<TokenResponse>('/auth/register', payload)
    return data
  },
  async login(payload: LoginPayload): Promise<TokenResponse> {
    const { data } = await api.post<TokenResponse>('/auth/login', payload)
    return data
  },
  async me(): Promise<User> {
    const { data } = await api.get<User>('/auth/me')
    return data
  },
  async updateProfile(payload: { full_name?: string; password?: string }): Promise<User> {
    const { data } = await api.put<User>('/auth/me', payload)
    return data
  },
}
