import type { TaskFormValues } from './TaskForm'
import { TaskFormDialog } from './TaskFormDialog'
import type { Workflow } from '../../../types/workflow'
import type { TeamMember } from '../../../types/user'

interface CreateTaskDialogProps {
  open: boolean
  submitting: boolean
  canManage: boolean
  workflows: Workflow[]
  teamMembers?: TeamMember[]
  loadingTeamMembers?: boolean
  onClose: () => void
  onSubmit: (values: TaskFormValues) => Promise<void>
}

export function CreateTaskDialog(props: CreateTaskDialogProps) {
  return (
    <TaskFormDialog
      {...props}
      title="Kreiranje zadatka"
      mode="create"
    />
  )
}
