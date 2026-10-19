import { api } from './api'
import type { VinDecodeResult } from '../types/vin'

export const vinService = {
  async decode(vin: string): Promise<VinDecodeResult> {
    const { data } = await api.post<VinDecodeResult>('/vin/decode', { vin })
    return data
  },
}
