import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { createProject, fetchProjectById, updateProject } from '../../api/projects'
import { useAuth } from '../../auth/AuthContext'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { TextInput } from '../../components/ui/TextInput'
import type { Project } from '../../types/project'

type Mode = 'create' | 'edit'

function toDateInputValue(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toISOString().slice(0, 10)
}

function toIsoDateOrUndefined(value: string): string | undefined {
  if (!value) return undefined
  const date = new Date(`${value}T00:00:00`)
  return Number.isNaN(date.getTime()) ? undefined : date.toISOString()
}

export function ProjectFormPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()

  const mode: Mode = useMemo(() => (projectId ? 'edit' : 'create'), [projectId])
  const canManage = user?.role === 'MANAGER'

  const [loading, setLoading] = useState(mode === 'edit')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  const loadProject = useCallback(async () => {
    if (mode !== 'edit' || !projectId) return
    setLoading(true)
    setError(null)
    try {
      const project = await fetchProjectById(projectId)
      setName(project.name ?? '')
      setDescription(project.description ?? '')
      setStartDate(toDateInputValue(project.startDate))
      setEndDate(toDateInputValue(project.endDate))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje projekta nije uspelo')
    } finally {
      setLoading(false)
    }
  }, [mode, projectId])

  useEffect(() => {
    void loadProject()
  }, [loadProject])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!canManage) {
      setError('Nemate dozvolu za ovu akciju.')
      return
    }

    const payload: Project = {
      ...(mode === 'edit' ? { id: projectId } : null),
      name: name.trim(),
      description: description.trim(),
      startDate: toIsoDateOrUndefined(startDate),
      endDate: toIsoDateOrUndefined(endDate),
    }

    setSaving(true)
    setError(null)
    try {
      if (mode === 'create') {
        await createProject(payload)
      } else if (projectId) {
        await updateProject(projectId, payload)
      }
      navigate('/projects', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Čuvanje projekta nije uspelo')
    } finally {
      setSaving(false)
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
              {mode === 'create' ? 'Novi projekat' : 'Izmena projekta'}
            </h1>
            <p className="mt-2 text-sm text-ink-subtle">
              {mode === 'create'
                ? 'Kreirajte novi projekat.'
                : 'Izmenite osnovne podatke o projektu.'}
            </p>
          </div>
          <Link to="/projects" className="inline-flex">
            <Button variant="secondary" type="button">
              Nazad
            </Button>
          </Link>
        </header>

        {error && (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        )}

        <section className="rounded-lg border border-hairline bg-surface-1 p-4 md:p-6">
          {loading ? (
            <p className="m-0 text-sm text-ink-subtle">Učitavanje…</p>
          ) : (
            <form onSubmit={(e) => void handleSubmit(e)} className="grid max-w-lg gap-4">
              <TextInput
                label="Naziv"
                name="name"
                required
                value={name}
                disabled={!canManage || saving}
                onChange={(e) => setName(e.target.value)}
              />
              <TextInput
                label="Opis"
                name="description"
                value={description}
                disabled={!canManage || saving}
                onChange={(e) => setDescription(e.target.value)}
              />
              <TextInput
                label="Početak realizacije"
                name="startDate"
                type="date"
                className="[color-scheme:dark]"
                value={startDate}
                disabled={!canManage || saving}
                onChange={(e) => setStartDate(e.target.value)}
              />
              <TextInput
                label="Rok završetka"
                name="endDate"
                type="date"
                className="[color-scheme:dark]"
                value={endDate}
                disabled={!canManage || saving}
                onChange={(e) => setEndDate(e.target.value)}
              />

              {canManage ? (
                <div className="flex flex-wrap items-center gap-3">
                  <Button
                    type="submit"
                    icon={mode === 'create' ? 'add' : 'edit'}
                    disabled={saving}
                  >
                    {saving ? 'Čuvanje…' : mode === 'create' ? 'Kreiraj' : 'Sačuvaj'}
                  </Button>
                  <Link to="/projects" className="inline-flex">
                    <Button variant="secondary" type="button" disabled={saving}>
                      Otkaži
                    </Button>
                  </Link>
                </div>
              ) : (
                <p className="m-0 text-sm text-ink-subtle">
                  Ovu formu mogu koristiti samo korisnici sa ulogom MENADŽER.
                </p>
              )}
            </form>
          )}
        </section>
      </div>
    </AppShell>
  )
}
