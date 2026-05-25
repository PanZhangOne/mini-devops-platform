import * as DialogPrimitive from '@radix-ui/react-dialog'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'

export const Dialog = DialogPrimitive.Root
export const DialogTrigger = DialogPrimitive.Trigger

interface DialogContentProps extends React.ComponentPropsWithoutRef<typeof DialogPrimitive.Content> {
  title?: string
  description?: string
}

export function DialogContent({ title, description, children, className, ...props }: DialogContentProps) {
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40 animate-in fade-in" />
      <DialogPrimitive.Content
        className={cn(
          'fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 z-50',
          'bg-[var(--color-surface)] rounded-[var(--radius-xl)] shadow-2xl',
          'w-full max-w-lg p-6 focus:outline-none',
          'animate-in fade-in slide-in-from-bottom-4',
          className,
        )}
        {...props}
      >
        <div className="flex items-center justify-between mb-4">
          {title && (
            <DialogPrimitive.Title className="text-base font-semibold text-[var(--color-text)]">
              {title}
            </DialogPrimitive.Title>
          )}
          <DialogPrimitive.Close className="ml-auto rounded-md p-1 hover:bg-[var(--color-surface-3)] transition-colors">
            <X size={16} className="text-[var(--color-text-muted)]" />
          </DialogPrimitive.Close>
        </div>
        {description && (
          <DialogPrimitive.Description className="text-sm text-[var(--color-text-muted)] mb-4">
            {description}
          </DialogPrimitive.Description>
        )}
        {children}
      </DialogPrimitive.Content>
    </DialogPrimitive.Portal>
  )
}
