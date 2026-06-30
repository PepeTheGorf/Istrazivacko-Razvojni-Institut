import { Button } from '../../../components/ui/Button'
import type { Project } from '../../../types/project'

interface DeleteProjectDialogProps {
  project: Project
  deleting: boolean
  onCancel: () => void
  onConfirm: () => void
}

export function DeleteProjectDialog({
  project,
  deleting,
  onCancel,
  onConfirm,
}: DeleteProjectDialogProps) {
  return (
    <div className="fixed inset-0 z-40 grid place-items-center bg-black/60 p-4">
      <div className="w-full max-w-lg rounded-lg border border-hairline bg-surface-1 p-6">
        <h2 className="m-0 text-lg font-semibold text-ink">Potvrda brisanja</h2>
        <p className="mt-3 mb-6 text-sm text-ink-muted">
          Da li ste sigurni da želite da obrišete projekat <strong>{project.name}</strong>?
        </p>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={onCancel} disabled={deleting}>
            Otkaži
          </Button>
          <Button
            variant="delete"
            icon="delete"
            className="border border-error/45 text-error hover:bg-error/10 hover:text-error"
            onClick={onConfirm}
            disabled={deleting}
          >
            {deleting ? 'Brisanje...' : 'Obriši'}
          </Button>
        </div>
      </div>
    </div>
  )
}

