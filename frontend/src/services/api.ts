import axios from 'axios'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api'

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
})

const TOKEN_KEY = 'vehicle_platform_token'

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

api.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export interface ApiErrorPayload {
  error?: string
  message: string
  detail?: string
  status: number
}

export function extractApiError(error: unknown): ApiErrorPayload {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status ?? 0
    const data = error.response?.data as { error?: string; message?: string; detail?: string } | undefined

    if (!error.response) {
      return { status: 0, message: "Impossible de contacter le serveur. Vérifiez qu'il est démarré." }
    }
    if (status === 429) {
      return { status, message: 'Quota de requêtes dépassé. Merci de réessayer dans quelques instants.' }
    }
    if (status >= 500) {
      return { status, message: data?.message || 'Erreur serveur. Réessayez plus tard.' }
    }
    return {
      status,
      message: data?.message || (typeof data?.detail === 'string' ? data.detail : undefined) || 'Une erreur est survenue.',
      error: data?.error,
    }
  }
  return { status: 0, message: 'Une erreur inattendue est survenue.' }
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      setStoredToken(null)
    }
    return Promise.reject(error)
  }
)
