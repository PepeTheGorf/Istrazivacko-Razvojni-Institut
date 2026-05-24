import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { createProject, fetchHealthCheck, fetchProjects } from '../api/projects'
import { AppShell } from '../components/layout/AppShell'
import { Button } from '../components/ui/Button'
import { TextInput } from '../components/ui/TextInput'
import type { Project } from '../types/project'

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([])
  const [health, setHealth] = useState<string>('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  const loadProjects = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [projectList, healthMessage] = await Promise.all([
        fetchProjects(),
        fetchHealthCheck(),
      ])
      setProjects(projectList)
      setHealth(healthMessage)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje podataka nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadProjects()
  }, [loadProjects])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      await createProject({ name, description })
      setName('')
      setDescription('')
      await loadProjects()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Kreiranje projekta nije uspelo')
    }
  }

  return (
    <AppShell>
      <div className="mx-auto grid max-w-4xl gap-6">
        <header className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="m-0 mb-2 text-[13px] font-medium tracking-wide text-ink-subtle uppercase">
              Projekti
            </p>
            <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
              Novi projekat
            </h1>
            <p className="mt-2 text-sm text-ink-subtle">
              Test stranica za kreiranje projekata kroz gateway servis
            </p>
          </div>
          {health && (
            <span className="rounded-full border border-success/30 bg-success/10 px-2.5 py-1.5 text-xs text-success">
              Gateway: {health}
            </span>
          )}
        </header>

        {error && (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        )}

        <section className="rounded-lg border border-hairline bg-surface-1 p-4 md:p-6">
          <h2 className="m-0 mb-4 text-lg font-semibold tracking-tight">Kreiraj projekat</h2>
          <form onSubmit={(e) => void handleSubmit(e)} className="grid max-w-lg gap-4">
            <TextInput
              label="Naziv"
              name="name"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <TextInput
              label="Opis"
              name="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
            <Button type="submit">Kreiraj</Button>
          </form>
        </section>

        <section className="rounded-lg border border-hairline bg-surface-1 p-4 md:p-6">
          <div className="mb-4 flex flex-col items-stretch justify-between gap-3 sm:flex-row sm:items-center">
            <h2 className="m-0 text-lg font-semibold tracking-tight">Lista projekata</h2>
            <Button variant="secondary" type="button" onClick={() => void loadProjects()}>
              Osveži
            </Button>
          </div>
          {loading ? (
            <p className="m-0 text-ink-subtle">Učitavanje…</p>
          ) : projects.length === 0 ? (
            <p className="m-0 text-ink-subtle">Još nema projekata.</p>
          ) : (
            <ul className="m-0 grid list-none gap-2 p-0">
              {projects.map((project) => (
                <li
                  key={project.id ?? project.name}
                  className="grid gap-0.5 rounded-md border border-hairline bg-surface-2 p-3"
                >
                  <strong className="text-sm text-ink">{project.name}</strong>
                  {project.description && (
                    <span className="text-[13px] text-ink-subtle">{project.description}</span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </AppShell>
  )
}
