import type { TaskFormValues } from './TaskForm'
import { TaskFormDialog } from './TaskFormDialog'
import type { Workflow } from '../../../types/workflow'
import type { TeamMember } from '../../../types/user'
import type { TaskDateConstraints } from '../../../lib/validateTaskDates'

interface CreateTaskDialogProps {
  open: boolean
  submitting: boolean
  canManage: boolean
  workflows: Workflow[]
  teamMembers?: TeamMember[]
  loadingTeamMembers?: boolean
  dateConstraints?: TaskDateConstraints
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
