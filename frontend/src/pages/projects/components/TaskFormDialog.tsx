import { Button } from '../../../components/ui/Button'
import { TaskForm, type TaskFormValues } from './TaskForm'
import type { Workflow } from '../../../types/workflow'

interface TaskFormDialogProps {
  open: boolean
  title: string
  subtitle?: string
  mode: 'create' | 'edit'
  submitting: boolean
  canManage: boolean
  workflows: Workflow[]
  initialValues?: TaskFormValues
  createSubmitLabel?: string
  onClose: () => void
  onSubmit: (values: TaskFormValues) => Promise<void>
}

export function TaskFormDialog({
  open,
  title,
  subtitle,
  mode,
  submitting,
  canManage,
  workflows,
  initialValues,
  createSubmitLabel,
  onClose,
  onSubmit,
}: TaskFormDialogProps) {
  if (!open) return null

  return (
    <div className="scrollbar-dark fixed inset-0 z-40 overflow-y-auto bg-black/60 p-4">
      <div className="mx-auto my-4 w-full max-w-3xl rounded-lg border border-hairline bg-surface-1">
        <div className="flex items-start justify-between gap-3 border-b border-hairline px-4 py-4 md:px-6">
          <div className="min-w-0">
            <h2 className="m-0 text-lg font-semibold text-ink">{title}</h2>
            {subtitle ? <p className="m-0 mt-1 text-sm text-ink-subtle">{subtitle}</p> : null}
          </div>
          <Button variant="secondary" onClick={onClose} disabled={submitting} className="shrink-0">
            Zatvori
          </Button>
        </div>
        <div className="scrollbar-dark max-h-[78vh] overflow-y-auto p-4 md:p-6">
          <TaskForm
            embedded
            mode={mode}
            submitting={submitting}
            canManage={canManage}
            workflows={workflows}
            initialValues={initialValues}
            createSubmitLabel={createSubmitLabel}
            onSubmit={onSubmit}
            onCancel={onClose}
          />
        </div>
      </div>
    </div>
  )
}
