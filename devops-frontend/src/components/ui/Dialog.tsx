import * as DialogPrimitive from '@radix-ui/react-dialog'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'

export const Dialog = DialogPrimitive.Root
export const DialogTrigger = DialogPrimitive.Trigger

interface DialogContentProps extends React.ComponentPropsWithoutRef<typeof DialogPrimitive.Content> {
  title?: string
  description?: string
  headerActions?: React.ReactNode
  footer?: React.ReactNode
  contentClassName?: string
}

export function DialogContent({
  title,
  description,
  headerActions,
  footer,
  contentClassName,
  children,
  className,
  ...props
}: DialogContentProps) {
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40 animate-in fade-in" />
      <DialogPrimitive.Content
        className={cn(
          'fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 z-50',
          'bg-[var(--color-surface)] rounded-[var(--radius-xl)] shadow-2xl',
          'flex max-h-[85vh] w-full max-w-lg flex-col overflow-hidden p-6 focus:outline-none',
          'animate-in fade-in slide-in-from-bottom-4',
          className,
        )}
        {...props}
      >
        {(title || description || headerActions) && (
          <div className="mb-4 flex items-start gap-3">
            <div className="min-w-0 flex-1">
              {title && (
                <DialogPrimitive.Title className="text-base font-semibold text-[var(--color-text)]">
                  {title}
                </DialogPrimitive.Title>
              )}
              {description && (
                <DialogPrimitive.Description className="mt-1 text-sm text-[var(--color-text-muted)]">
                  {description}
                </DialogPrimitive.Description>
              )}
            </div>
            {headerActions && <div className="flex shrink-0 items-center gap-2">{headerActions}</div>}
            <DialogPrimitive.Close className="ml-auto shrink-0 rounded-md p-1 transition-colors hover:bg-[var(--color-surface-3)]">
              <X size={16} className="text-[var(--color-text-muted)]" />
            </DialogPrimitive.Close>
          </div>
        )}
        <div className={cn('min-h-0 flex-1', contentClassName)}>{children}</div>
        {footer && <div className="mt-4 flex shrink-0 items-center justify-end gap-2">{footer}</div>}
      </DialogPrimitive.Content>
    </DialogPrimitive.Portal>
  )
}
