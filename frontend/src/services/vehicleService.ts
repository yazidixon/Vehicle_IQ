import { api } from './api'
import type { PaginatedResponse, Vehicle, VehicleSearchFilters, VehicleSourceData, VehicleSummary } from '../types/vehicle'

export const vehicleService = {
  async search(filters: VehicleSearchFilters): Promise<PaginatedResponse<VehicleSummary>> {
    const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== undefined && v !== ''))
    const { data } = await api.get<PaginatedResponse<VehicleSummary>>('/vehicles', { params })
    return data
  },
  async getById(id: string): Promise<Vehicle> {
    const { data } = await api.get<Vehicle>(`/vehicles/${id}`)
    return data
  },
  async getSources(id: string): Promise<VehicleSourceData[]> {
    const { data } = await api.get<VehicleSourceData[]>(`/vehicles/${id}/sources`)
    return data
  },
  async listMakes(): Promise<string[]> {
    const { data } = await api.get<string[]>('/vehicles/meta/makes')
    return data
  },
  async listModels(make: string): Promise<string[]> {
    const { data } = await api.get<string[]>('/vehicles/meta/models', { params: { make } })
    return data
  },
}
