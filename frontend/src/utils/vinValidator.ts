export interface VinValidationResult {
  valid: boolean
  errors: string[]
}

/** Mirrors the backend's structural validation for instant UX feedback. The
 * backend remains the source of truth — this never replaces server validation. */
export function validateVinFormat(rawVin: string): VinValidationResult {
  const vin = (rawVin || '').trim().toUpperCase()
  const errors: string[] = []

  if (vin.length !== 17) {
    errors.push(`Le VIN doit contenir exactement 17 caractères (reçu : ${vin.length}).`)
  }
  if (/[IOQ]/.test(vin)) {
    errors.push('Le VIN ne peut pas contenir les lettres I, O ou Q.')
  }
  if (!/^[A-Z0-9]*$/.test(vin)) {
    errors.push('Le VIN ne doit contenir que des lettres et des chiffres (alphanumérique).')
  }

  return { valid: errors.length === 0, errors }
}
