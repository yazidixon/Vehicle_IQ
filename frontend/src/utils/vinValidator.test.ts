import { describe, expect, it } from 'vitest'
import { validateVinFormat } from './vinValidator'

describe('validateVinFormat', () => {
  it('accepts a well-formed 17-character VIN', () => {
    const result = validateVinFormat('1HGCM82633A004352')
    expect(result.valid).toBe(true)
    expect(result.errors).toHaveLength(0)
  })

  it('rejects a VIN shorter than 17 characters', () => {
    const result = validateVinFormat('1HGCM82633A00435')
    expect(result.valid).toBe(false)
    expect(result.errors.length).toBeGreaterThan(0)
  })

  it('rejects a VIN longer than 17 characters', () => {
    const result = validateVinFormat('1HGCM82633A0043522')
    expect(result.valid).toBe(false)
  })

  it.each(['I', 'O', 'Q'])('rejects a VIN containing the forbidden letter %s', (letter) => {
    const vin = `1HGCM8263${letter}A004352`
    const result = validateVinFormat(vin)
    expect(result.valid).toBe(false)
  })

  it('rejects non-alphanumeric characters', () => {
    const result = validateVinFormat('1HG-M82633A004352')
    expect(result.valid).toBe(false)
  })

  it('trims whitespace and uppercases before validating', () => {
    const result = validateVinFormat('  1hgcm82633a004352  ')
    expect(result.valid).toBe(true)
  })
})
