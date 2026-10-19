import { describe, expect, it } from 'vitest'
import { formatCurrency, formatValue, NOT_AVAILABLE } from './formatters'

describe('formatValue', () => {
  it('returns the sentinel for null values', () => {
    expect(formatValue(null)).toBe(NOT_AVAILABLE)
  })

  it('returns the sentinel for undefined values', () => {
    expect(formatValue(undefined)).toBe(NOT_AVAILABLE)
  })

  it('never renders an ambiguous empty string for a missing value', () => {
    expect(formatValue('')).toBe(NOT_AVAILABLE)
  })

  it('formats a plain value without unit', () => {
    expect(formatValue(190)).toBe('190')
  })

  it('appends the unit when provided', () => {
    expect(formatValue(190, 'ch')).toBe('190 ch')
  })
})

describe('formatCurrency', () => {
  it('returns the sentinel for a missing price', () => {
    expect(formatCurrency(null)).toBe(NOT_AVAILABLE)
  })

  it('formats a known price in EUR by default', () => {
    expect(formatCurrency(48900)).toContain('48')
    expect(formatCurrency(48900)).toContain('900')
  })
})
