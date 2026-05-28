import { AppShell } from '../../components/layout/AppShell'
import { DeleteWorkflowDialog } from './components/DeleteWorkflowDialog'
import { WorkflowList } from './components/WorkflowList'
import { WorkflowListToolbar } from './components/WorkflowListToolbar'
import { WorkflowsPageHeader } from './components/WorkflowsPageHeader'
import { useWorkflowsPage } from './hooks/useWorkflowsPage'

export function WorkflowsPage() {
  const {
    loading,
    error,
    search,
    setSearch,
    sortBy,
    setSortBy,
    filtered,
    workflowToDelete,
    setWorkflowToDelete,
    deleting,
    confirmDelete,
  } = useWorkflowsPage()

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <WorkflowsPageHeader />

        <WorkflowListToolbar
          search={search}
          onSearchChange={setSearch}
          sortBy={sortBy}
          onSortByChange={setSortBy}
        />

        {error && (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        )}

        <WorkflowList
          workflows={filtered}
          loading={loading}
          onDelete={setWorkflowToDelete}
        />
      </div>

      {workflowToDelete && (
        <DeleteWorkflowDialog
          workflow={workflowToDelete}
          deleting={deleting}
          onCancel={() => setWorkflowToDelete(null)}
          onConfirm={() => void confirmDelete()}
        />
      )}
    </AppShell>
  )
}
