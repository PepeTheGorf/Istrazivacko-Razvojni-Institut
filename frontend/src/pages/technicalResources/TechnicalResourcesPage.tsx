import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { DeleteTechnicalResourceDialog } from './components/DeleteTechnicalResourceDialog'
import { TechnicalResourceFormDialog } from './components/TechnicalResourceFormDialog'
import { TechnicalResourceList } from './components/TechnicalResourceList'
import { useTechnicalResourcesPage } from './hooks/useTechnicalResourcesPage'

export function TechnicalResourcesPage() {
  const {
    loading,
    error,
    search,
    setSearch,
    filtered,
    formOpen,
    setFormOpen,
    editingResource,
    openCreate,
    openEdit,
    saving,
    saveResource,
    resourceToDelete,
    setResourceToDelete,
    deleting,
    confirmDelete,
  } = useTechnicalResourcesPage()

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <header className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="m-0 mb-2 text-[13px] font-medium tracking-wide text-ink-subtle uppercase">
              Resursi
            </p>
            <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
              Tehnički resursi
            </h1>
            <p className="m-0 mt-2 text-sm text-ink-subtle">
              Kreirajte i održavajte resurse koje menadžeri dodeljuju zadacima.
            </p>
          </div>
          <Button icon="add" onClick={openCreate}>
            Novi resurs
          </Button>
        </header>

        <label className="grid max-w-md gap-1">
          <span className="text-[13px] font-medium text-ink-muted">Pretraga</span>
          <input
            type="text"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Naziv ili opis..."
            className="min-h-10 rounded-md border border-hairline bg-surface-2 px-3 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
          />
        </label>

        {error ? (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        ) : null}

        <TechnicalResourceList
          resources={filtered}
          loading={loading}
          onEdit={openEdit}
          onDelete={setResourceToDelete}
        />
      </div>

      <TechnicalResourceFormDialog
        open={formOpen}
        resource={editingResource}
        saving={saving}
        onClose={() => setFormOpen(false)}
        onSave={saveResource}
      />

      {resourceToDelete ? (
        <DeleteTechnicalResourceDialog
          resource={resourceToDelete}
          deleting={deleting}
          onCancel={() => setResourceToDelete(null)}
          onConfirm={() => void confirmDelete()}
        />
      ) : null}
    </AppShell>
  )
}
