import { api } from './api'
import type { ComparisonResult, SavedComparison } from '../types/comparison'

export const comparisonService = {
  async compute(vehicleIds: string[]): Promise<ComparisonResult> {
    const { data } = await api.post<ComparisonResult>('/comparisons/compute', { vehicle_ids: vehicleIds })
    return data
  },
  async save(name: string, vehicleIds: string[]): Promise<SavedComparison> {
    const { data } = await api.post<SavedComparison>('/comparisons', { name, vehicle_ids: vehicleIds })
    return data
  },
  async list(): Promise<SavedComparison[]> {
    const { data } = await api.get<SavedComparison[]>('/comparisons')
    return data
  },
  async getById(id: string): Promise<SavedComparison> {
    const { data } = await api.get<SavedComparison>(`/comparisons/${id}`)
    return data
  },
  async rename(id: string, name: string): Promise<SavedComparison> {
    const { data } = await api.put<SavedComparison>(`/comparisons/${id}`, { name })
    return data
  },
  async remove(id: string): Promise<void> {
    await api.delete(`/comparisons/${id}`)
  },
}
