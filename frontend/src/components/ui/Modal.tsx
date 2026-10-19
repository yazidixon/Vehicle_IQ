import { X } from 'lucide-react'
import type { ReactNode } from 'react'
import { useEffect } from 'react'

export function Modal({
  title,
  onClose,
  children,
  footer,
}: {
  title: string
  onClose: () => void
  children: ReactNode
  footer?: ReactNode
}) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm animate-fade-in" onClick={onClose} />
      <div className="relative card-elevated w-full max-w-md animate-fade-in">
        <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--color-border)]">
          <h3 className="font-display font-semibold text-[var(--color-text)]">{title}</h3>
          <button onClick={onClose} className="text-[var(--color-text-faint)] hover:text-[var(--color-text)]">
            <X size={18} />
          </button>
        </div>
        <div className="px-5 py-4">{children}</div>
        {footer && <div className="px-5 py-4 border-t border-[var(--color-border)] flex justify-end gap-2">{footer}</div>}
      </div>
    </div>
  )
}
