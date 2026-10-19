import { AlertTriangle, RotateCw } from 'lucide-react'

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center text-center py-16 px-6 card border-[var(--color-danger-500)]/30">
      <AlertTriangle size={32} className="text-[var(--color-danger-400)] mb-3" />
      <h3 className="font-display text-lg font-semibold text-[var(--color-text)]">Une erreur est survenue</h3>
      <p className="mt-1.5 text-sm text-[var(--color-text-muted)] max-w-sm">{message}</p>
      {onRetry && (
        <button onClick={onRetry} className="btn-secondary mt-5 px-4 py-2 text-sm flex items-center gap-2">
          <RotateCw size={14} /> Réessayer
        </button>
      )}
    </div>
  )
}
