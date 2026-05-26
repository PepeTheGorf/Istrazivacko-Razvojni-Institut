import { useNavigate } from 'react-router-dom'
import { createWorkflow } from '../../api/workflows'
import { AppShell } from '../../components/layout/AppShell'
import { WorkflowFormWizard } from '../../components/workflow/WorkflowFormWizard'

export function WorkflowCreateWizardPage() {
  const navigate = useNavigate()

  return (
    <AppShell>
      <WorkflowFormWizard
        title="Novi Tok Rada"
        submitLabel="Kreiraj"
        submitIcon="add"
        onCancel={() => navigate('/workflows')}
        onSubmit={async (workflow) => {
          await createWorkflow(workflow)
          navigate('/workflows')
        }}
      />
    </AppShell>
  )
}
