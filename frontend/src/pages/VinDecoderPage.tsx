import { useState, type FormEvent } from 'react'
import { AlertCircle, Loader2, ScanLine } from 'lucide-react'
import { vinService } from '../services/vinService'
import { validateVinFormat } from '../utils/vinValidator'
import { extractApiError } from '../services/api'
import { DataSourceBadge } from '../components/ui/Badge'
import { NOT_AVAILABLE, type VinDecodeResult } from '../types/vin'

const FIELD_LABELS: { key: keyof VinDecodeResult; label: string }[] = [
  { key: 'country_of_manufacture', label: 'Pays de fabrication' },
  { key: 'manufacturer', label: 'Constructeur' },
  { key: 'plant', label: 'Usine de production' },
  { key: 'model_year', label: 'Année modèle' },
  { key: 'make', label: 'Marque' },
  { key: 'model', label: 'Modèle' },
  { key: 'series', label: 'Série' },
  { key: 'trim', label: 'Finition' },
  { key: 'engine_code', label: 'Code moteur' },
  { key: 'body_class', label: 'Type de carrosserie' },
]

export function VinDecoderPage() {
  const [vin, setVin] = useState('')
  const [formatErrors, setFormatErrors] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)
  const [result, setResult] = useState<VinDecodeResult | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setApiError(null)
    setResult(null)
    const validation = validateVinFormat(vin)
    setFormatErrors(validation.errors)
    if (!validation.valid) return

    setLoading(true)
    try {
      const data = await vinService.decode(vin.trim().toUpperCase())
      setResult(data)
    } catch (err) {
      setApiError(extractApiError(err).message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
      <div className="text-center mb-8">
        <div className="inline-flex h-12 w-12 rounded-2xl bg-[var(--color-primary-500)]/10 items-center justify-center text-[var(--color-primary-400)] mb-3">
          <ScanLine size={22} />
        </div>
        <h1 className="font-display text-2xl font-bold text-[var(--color-text)]">Décodeur VIN</h1>
        <p className="text-sm text-[var(--color-text-muted)] mt-1">
          Décodage en temps réel via l'API officielle NHTSA (États-Unis).
        </p>
      </div>

      <form onSubmit={handleSubmit} className="card-elevated p-5 flex flex-col sm:flex-row gap-3">
        <input
          value={vin}
          onChange={(e) => setVin(e.target.value.toUpperCase())}
          maxLength={17}
          placeholder="Ex : 1HGCM82633A004352 (17 caractères)"
          className="input-field flex-1 px-4 py-3 text-sm font-mono-tech tracking-wider"
        />
        <button type="submit" disabled={loading} className="btn-primary px-6 py-3 text-sm flex items-center justify-center gap-2">
          {loading ? <Loader2 size={16} className="animate-spin" /> : <ScanLine size={16} />}
          Décoder
        </button>
      </form>
      <p className="text-xs text-[var(--color-text-faint)] mt-2">{vin.length}/17 caractères — pas de I, O ni Q.</p>

      {formatErrors.length > 0 && (
        <div className="mt-4 text-sm text-[var(--color-danger-400)] bg-[var(--color-danger-500)]/10 border border-[var(--color-danger-500)]/30 rounded-lg px-4 py-3 space-y-1">
          {formatErrors.map((err) => <p key={err} className="flex items-center gap-2"><AlertCircle size={14} /> {err}</p>)}
        </div>
      )}

      {apiError && (
        <div className="mt-4 text-sm text-[var(--color-danger-400)] bg-[var(--color-danger-500)]/10 border border-[var(--color-danger-500)]/30 rounded-lg px-4 py-3">
          {apiError}
        </div>
      )}

      {result && (
        <div className="mt-6 card-elevated p-6 animate-fade-in">
          <div className="flex items-center justify-between mb-5">
            <div>
              <p className="text-xs text-[var(--color-text-muted)] font-mono-tech">{result.vin}</p>
              <h2 className="font-display font-semibold text-lg text-[var(--color-text)]">
                {result.make !== NOT_AVAILABLE ? `${result.make} ${result.model !== NOT_AVAILABLE ? result.model : ''}` : 'Véhicule décodé'}
              </h2>
            </div>
            <DataSourceBadge isDemoData={result.is_demo_data} />
          </div>

          {result.warnings.length > 0 && (
            <div className="mb-4 text-xs text-[var(--color-amber-400)] bg-[var(--color-amber-500)]/10 border border-[var(--color-amber-500)]/30 rounded-lg px-3 py-2">
              {result.warnings.join(' ')}
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-4">
            {FIELD_LABELS.map(({ key, label }) => {
              const value = result[key] as string
              const isMissing = value === NOT_AVAILABLE
              return (
                <div key={key} className="flex flex-col">
                  <span className="text-xs text-[var(--color-text-muted)]">{label}</span>
                  <span className={`text-sm font-medium ${isMissing ? 'text-[var(--color-text-faint)] italic' : 'text-[var(--color-text)]'}`}>
                    {value}
                  </span>
                </div>
              )
            })}
          </div>

          <p className="text-xs text-[var(--color-text-faint)] mt-5 pt-4 border-t border-[var(--color-border)]">
            Source : {result.source_provider === 'nhtsa' ? 'NHTSA vPIC (API officielle)' : result.source_provider}
          </p>
        </div>
      )}
    </div>
  )
}
