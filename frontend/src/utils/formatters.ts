export const NOT_AVAILABLE = 'Data not available'

export function formatValue(value: string | number | null | undefined, unit?: string): string {
  if (value === null || value === undefined || value === '') return NOT_AVAILABLE
  return unit ? `${value} ${unit}` : `${value}`
}

export function formatCurrency(value: number | null | undefined, currency = 'EUR'): string {
  if (value === null || value === undefined) return NOT_AVAILABLE
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency, maximumFractionDigits: 0 }).format(value)
}

// Taux fixe demandé : 1 EUR = 10 MAD.
const EUR_TO_MAD_RATE = 10

export function formatPriceInMad(priceEur: number | null | undefined): string {
  if (priceEur === null || priceEur === undefined) return NOT_AVAILABLE
  return formatCurrency(priceEur * EUR_TO_MAD_RATE, 'MAD')
}

export function formatDate(iso: string | null | undefined): string {
  if (!iso) return NOT_AVAILABLE
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso))
}

export function formatDateShort(iso: string | null | undefined): string {
  if (!iso) return NOT_AVAILABLE
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(new Date(iso))
}
