import { AppShell } from '../../components/layout/AppShell'
import { DeleteProjectDialog } from './components/DeleteProjectDialog'
import { ProjectList } from './components/ProjectList'
import { ProjectsPageHeader } from './components/ProjectsPageHeader'
import { useProjectsPage } from './hooks/useProjectsPage'

export function ProjectsPage() {
  const {
    loading,
    error,
    projects,
    projectToDelete,
    setProjectToDelete,
    deleting,
    confirmDelete,
    canManage,
  } = useProjectsPage()

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <ProjectsPageHeader canManage={canManage} />

        {error && (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        )}

        <ProjectList
          projects={projects}
          loading={loading}
          canManage={canManage}
          onDelete={setProjectToDelete}
        />
      </div>

      {projectToDelete && (
        <DeleteProjectDialog
          project={projectToDelete}
          deleting={deleting}
          onCancel={() => setProjectToDelete(null)}
          onConfirm={() => void confirmDelete()}
        />
      )}
    </AppShell>
  )
}

