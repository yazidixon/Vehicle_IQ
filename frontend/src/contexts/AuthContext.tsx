import { createContext, useCallback, useEffect, useState, type ReactNode } from 'react'
import { authService } from '../services/authService'
import { getStoredToken, setStoredToken } from '../services/api'
import type { LoginPayload, RegisterPayload, User } from '../types/auth'

interface AuthContextValue {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (payload: LoginPayload) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => void
  refreshProfile: () => Promise<void>
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const loadProfile = useCallback(async () => {
    const token = getStoredToken()
    if (!token) {
      setUser(null)
      setIsLoading(false)
      return
    }
    try {
      const profile = await authService.me()
      setUser(profile)
    } catch {
      setStoredToken(null)
      setUser(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadProfile()
  }, [loadProfile])

  const login = useCallback(async (payload: LoginPayload) => {
    const result = await authService.login(payload)
    setStoredToken(result.access_token)
    setUser(result.user)
  }, [])

  const register = useCallback(async (payload: RegisterPayload) => {
    const result = await authService.register(payload)
    setStoredToken(result.access_token)
    setUser(result.user)
  }, [])

  const logout = useCallback(() => {
    setStoredToken(null)
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{ user, isLoading, isAuthenticated: !!user, login, register, logout, refreshProfile: loadProfile }}
    >
      {children}
    </AuthContext.Provider>
  )
}
