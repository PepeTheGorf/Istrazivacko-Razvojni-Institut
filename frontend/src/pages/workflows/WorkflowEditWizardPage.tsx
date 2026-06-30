import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { fetchWorkflowById, updateWorkflow } from '../../api/workflows'
import { AppShell } from '../../components/layout/AppShell'
import { WorkflowFormWizard } from '../../components/workflow/WorkflowFormWizard'
import { resolveWorkflowPhases } from '../../lib/workflowPhases'
import type { Workflow } from '../../types/workflow'

export function WorkflowEditWizardPage() {
  const { workflowId } = useParams<{ workflowId: string }>()
  const navigate = useNavigate()
  const [workflow, setWorkflow] = useState<Workflow | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadWorkflow = useCallback(async () => {
    if (!workflowId) {
      setError('Tok rada nije pronađen.')
      setLoading(false)
      return
    }

    setLoading(true)
    setError(null)
    try {
      const found = await fetchWorkflowById(Number(workflowId))
      setWorkflow(found)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje toka rada nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [workflowId])

  useEffect(() => {
    void loadWorkflow()
  }, [loadWorkflow])

  return (
    <AppShell>
      {loading ? (
        <p className="m-0 text-ink-subtle">Učitavanje...</p>
      ) : error || !workflow?.id ? (
        <div className="grid gap-4">
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error ?? 'Tok rada nije pronađen.'}
          </p>
        </div>
      ) : (
        <WorkflowFormWizard
          title="Izmeni Tok Rada"
          submitLabel="Sačuvaj"
          initial={{
            name: workflow.name,
            description: workflow.description ?? '',
            phases: resolveWorkflowPhases(workflow),
            transitionConditions: workflow.transitionConditions ?? [],
          }}
          onCancel={() => navigate('/workflows')}
          onSubmit={async (payload) => {
            await updateWorkflow(workflow.id!, payload)
            navigate('/workflows')
          }}
        />
      )}
    </AppShell>
  )
}
