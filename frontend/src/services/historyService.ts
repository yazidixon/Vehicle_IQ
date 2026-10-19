import { api } from './api'
import type { DashboardResponse, SearchHistoryEntry } from '../types/history'

export const historyService = {
  async list(): Promise<SearchHistoryEntry[]> {
    const { data } = await api.get<SearchHistoryEntry[]>('/search/history')
    return data
  },
}

export const dashboardService = {
  async get(): Promise<DashboardResponse> {
    const { data } = await api.get<DashboardResponse>('/dashboard')
    return data
  },
}
