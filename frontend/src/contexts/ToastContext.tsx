import { createContext, useCallback, useState, type ReactNode } from 'react'
import { CheckCircle2, XCircle, Info, X } from 'lucide-react'

export type ToastVariant = 'success' | 'error' | 'info'

interface Toast {
  id: number
  message: string
  variant: ToastVariant
}

interface ToastContextValue {
  showToast: (message: string, variant?: ToastVariant) => void
}

// eslint-disable-next-line react-refresh/only-export-components
export const ToastContext = createContext<ToastContextValue | undefined>(undefined)

let nextId = 1

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const showToast = useCallback((message: string, variant: ToastVariant = 'info') => {
    const id = nextId++
    setToasts((prev) => [...prev, { id, message, variant }])
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 4500)
  }, [])

  const dismiss = (id: number) => setToasts((prev) => prev.filter((t) => t.id !== id))

  const icons: Record<ToastVariant, ReactNode> = {
    success: <CheckCircle2 size={18} className="text-[var(--color-success-400)]" />,
    error: <XCircle size={18} className="text-[var(--color-danger-400)]" />,
    info: <Info size={18} className="text-[var(--color-primary-400)]" />,
  }

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-[calc(100%-2rem)] max-w-sm">
        {toasts.map((t) => (
          <div
            key={t.id}
            className="card-elevated animate-fade-in flex items-start gap-3 px-4 py-3 shadow-lg"
          >
            {icons[t.variant]}
            <p className="text-sm text-[var(--color-text)] flex-1">{t.message}</p>
            <button onClick={() => dismiss(t.id)} className="text-[var(--color-text-faint)] hover:text-[var(--color-text)]">
              <X size={16} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
