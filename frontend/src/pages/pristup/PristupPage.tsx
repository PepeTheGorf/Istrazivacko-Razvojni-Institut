import { useEffect, useState } from 'react'
import {
  fetchAllUsers,
  getDokumentAccess,
  getProjekatAccess,
  grantDokumentAccess,
  grantProjekatAccess,
  revokeDokumentAccess,
  revokeProjekatAccess,
  type KorisnikInfo,
} from '../../api/pristup'
import { fetchDocumentsByProject } from '../../api/documents'
import { fetchProjects } from '../../api/projects'
import { useAuth } from '../../auth/AuthContext'
import { AppShell } from '../../components/layout/AppShell'
import type { NivoPrava, PravaPristupa } from '../../types/pravaPristupa'
import type { Dokument } from '../../types/document'
import type { Project } from '../../types/project'

type Tab = 'PROJEKAT' | 'DOKUMENTI'

function NivoBadge({ nivo }: { nivo: NivoPrava }) {
  if (nivo === 'IZMENA') {
    return (
      <span className="rounded border border-green-500/60 px-2 py-0.5 text-xs text-green-400">
        čitanje i izmena
      </span>
    )
  }
  if (nivo === 'ZABRANA') {
    return (
      <span className="rounded border border-red-500/60 px-2 py-0.5 text-xs text-red-400">
        zabranjen pristup
      </span>
    )
  }
  return (
    <span className="rounded border border-blue-500/60 px-2 py-0.5 text-xs text-blue-400">
      samo čitanje
    </span>
  )
}

function formatDate(value: string) {
  try {
    return new Date(value).toLocaleDateString('sr-RS')
  } catch {
    return value
  }
}

function userLabel(u: KorisnikInfo) {
  return `${u.name} ${u.surname} (${u.email})`
}

interface AccessPanelProps {
  resourceType: 'DOKUMENT' | 'PROJEKAT'
  resourceId: string
  korisnici: KorisnikInfo[]
  currentUserId: string
}

function AccessPanel({ resourceType, resourceId, korisnici, currentUserId }: AccessPanelProps) {
  const [pristup, setPristup] = useState<PravaPristupa[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [newKorisnikId, setNewKorisnikId] = useState('')
  const [newNivo, setNewNivo] = useState<NivoPrava>('CITANJE')
  const [addLoading, setAddLoading] = useState(false)
  const [addError, setAddError] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editNivo, setEditNivo] = useState<NivoPrava>('CITANJE')
  const [editLoading, setEditLoading] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const data = resourceType === 'DOKUMENT'
        ? await getDokumentAccess(resourceId)
        : await getProjekatAccess(resourceId)
      setPristup(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Greška pri učitavanju pristupa')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
    setNewKorisnikId('')
    setNewNivo('CITANJE')
    setAddError(null)
    setEditingId(null)
  }, [resourceId, resourceType])

  async function handleAdd() {
    if (!newKorisnikId) return
    setAddLoading(true)
    setAddError(null)
    try {
      if (resourceType === 'DOKUMENT') {
        await grantDokumentAccess(resourceId, newKorisnikId, newNivo, currentUserId)
      } else {
        await grantProjekatAccess(resourceId, newKorisnikId, newNivo, currentUserId)
      }
      setNewKorisnikId('')
      setNewNivo('CITANJE')
      await load()
    } catch (err) {
      setAddError(err instanceof Error ? err.message : 'Greška pri dodavanju pristupa')
    } finally {
      setAddLoading(false)
    }
  }

  async function handleRevoke(korisnikId: string) {
    try {
      if (resourceType === 'DOKUMENT') {
        await revokeDokumentAccess(resourceId, korisnikId)
      } else {
        await revokeProjekatAccess(resourceId, korisnikId)
      }
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Greška pri uklanjanju pristupa')
    }
  }

  async function handleChangeNivo(korisnikId: string, noviNivo: NivoPrava) {
    setEditLoading(true)
    try {
      if (resourceType === 'DOKUMENT') {
        await grantDokumentAccess(resourceId, korisnikId, noviNivo, currentUserId)
      } else {
        await grantProjekatAccess(resourceId, korisnikId, noviNivo, currentUserId)
      }
      setEditingId(null)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Greška pri izmeni nivoa')
    } finally {
      setEditLoading(false)
    }
  }

  function resolveUserLabel(korisnikId: string) {
    const k = korisnici.find((u) => u.uuid === korisnikId || String(u.id) === korisnikId)
    return k ? userLabel(k) : korisnikId
  }

  const availableKorisnici = korisnici.filter(
    (k) => !pristup.some((p) => p.korisnikId === k.uuid || p.korisnikId === String(k.id)),
  )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-2 rounded-md border border-hairline bg-surface-2 p-3">
        <div className="text-xs font-medium text-ink-muted">Dodaj pristup</div>
        <div className="flex gap-2">
          <select
            value={newKorisnikId}
            onChange={(e) => setNewKorisnikId(e.target.value)}
            className="min-h-9 flex-1 rounded-md border border-hairline bg-surface-1 px-3 py-2 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
          >
            <option value="">Izaberi korisnika...</option>
            {availableKorisnici.map((k) => (
              <option key={k.id} value={String(k.id)}>
                {userLabel(k)} — {k.role}
              </option>
            ))}
          </select>
          <select
            value={newNivo}
            onChange={(e) => setNewNivo(e.target.value as NivoPrava)}
            className="min-h-9 rounded-md border border-hairline bg-surface-1 px-3 py-2 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
          >
            <option value="CITANJE">Samo čitanje</option>
            <option value="IZMENA">Čitanje i izmena</option>
            {resourceType === 'DOKUMENT' && (
              <option value="ZABRANA">Zabranjen pristup</option>
            )}
          </select>
          <button
            type="button"
            onClick={() => void handleAdd()}
            disabled={addLoading || !newKorisnikId}
            className="rounded-md border border-hairline bg-primary/20 px-4 py-2 text-sm text-primary hover:bg-primary/30 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
          >
            {addLoading ? 'Dodavanje…' : 'Dodaj'}
          </button>
        </div>
        {addError && <div className="text-xs text-[#ffb4b4]">{addError}</div>}
      </div>

      {error && (
        <div className="rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
          {error}
        </div>
      )}

      {loading ? (
        <div className="py-4 text-center text-sm text-ink-muted">Učitavanje...</div>
      ) : pristup.length === 0 ? (
        <div className="rounded-md border border-hairline bg-surface-2 px-4 py-3 text-sm text-ink-muted">
          Nema dodeljenih pristupa.
        </div>
      ) : (
        <div className="divide-y divide-hairline rounded-md border border-hairline">
          {pristup.map((item) => (
            <div key={item.id} className="flex flex-col gap-2 px-4 py-3">
              <div className="flex items-center justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <div className="truncate text-sm font-medium text-ink">
                    {resolveUserLabel(item.korisnikId)}
                  </div>
                  <div className="mt-0.5 text-xs text-ink-muted">
                    Dodao: {resolveUserLabel(item.dodeljivaoId)} &nbsp;·&nbsp; {formatDate(item.datumDodele)}
                  </div>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <NivoBadge nivo={item.nivo} />
                  <button
                    type="button"
                    onClick={() => {
                      if (editingId === item.id) {
                        setEditingId(null)
                      } else {
                        setEditingId(item.id)
                        setEditNivo(item.nivo)
                      }
                    }}
                    className="flex h-7 w-7 cursor-pointer items-center justify-center rounded-md text-ink-muted hover:bg-surface-2 hover:text-ink"
                    title="Izmeni nivo"
                  >
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden>
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" />
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </button>
                  <button
                    type="button"
                    onClick={() => void handleRevoke(item.korisnikId)}
                    className="flex h-7 w-7 cursor-pointer items-center justify-center rounded-md text-ink-muted hover:bg-error/15 hover:text-error"
                    title="Ukloni pristup"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden>
                      <path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </button>
                </div>
              </div>
              {editingId === item.id && (
                <div className="flex items-center gap-2 rounded-md border border-hairline bg-surface-2 px-3 py-2">
                  <select
                    value={editNivo}
                    onChange={(e) => setEditNivo(e.target.value as NivoPrava)}
                    disabled={editLoading}
                    className="min-h-8 flex-1 rounded border border-hairline bg-surface-1 px-2 py-1 text-sm text-ink focus:outline-none disabled:opacity-50"
                  >
                    <option value="CITANJE">Samo čitanje</option>
                    <option value="IZMENA">Čitanje i izmena</option>
                    {resourceType === 'DOKUMENT' && (
                      <option value="ZABRANA">Zabranjen pristup</option>
                    )}
                  </select>
                  <button
                    type="button"
                    onClick={() => void handleChangeNivo(item.korisnikId, editNivo)}
                    disabled={editLoading || editNivo === item.nivo}
                    className="rounded-md border border-hairline bg-primary/20 px-3 py-1 text-xs text-primary hover:bg-primary/30 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
                  >
                    {editLoading ? 'Čuvanje…' : 'Potvrdi'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingId(null)}
                    disabled={editLoading}
                    className="rounded-md border border-hairline px-3 py-1 text-xs text-ink-muted hover:bg-surface-2 cursor-pointer"
                  >
                    Otkaži
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

interface DocumentRowProps {
  document: Dokument
  korisnici: KorisnikInfo[]
  currentUserId: string
}

function DocumentRow({ document, korisnici, currentUserId }: DocumentRowProps) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="rounded-md border border-hairline bg-surface-1">
      <button
        type="button"
        onClick={() => setExpanded((v) => !v)}
        className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left hover:bg-surface-2 cursor-pointer rounded-md"
      >
        <span className="truncate text-sm font-medium text-ink">{document.naslov}</span>
        <svg
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="none"
          className={`shrink-0 text-ink-muted transition-transform ${expanded ? 'rotate-180' : ''}`}
          aria-hidden
        >
          <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>
      {expanded && (
        <div className="border-t border-hairline px-4 pb-4 pt-3">
          <AccessPanel
            resourceType="DOKUMENT"
            resourceId={document.id}
            korisnici={korisnici}
            currentUserId={currentUserId}
          />
        </div>
      )}
    </div>
  )
}

export function PristupPage() {
  const { user } = useAuth()
  const currentUserId = user ? String(user.id) : ''

  const [projects, setProjects] = useState<Project[]>([])
  const [korisnici, setKorisnici] = useState<KorisnikInfo[]>([])
  const [loadingInit, setLoadingInit] = useState(true)
  const [initError, setInitError] = useState<string | null>(null)

  const [selectedProject, setSelectedProject] = useState<Project | null>(null)
  const [activeTab, setActiveTab] = useState<Tab>('PROJEKAT')

  const [projectDocs, setProjectDocs] = useState<Dokument[]>([])
  const [docsLoading, setDocsLoading] = useState(false)
  const [docsError, setDocsError] = useState<string | null>(null)

  useEffect(() => {
    async function load() {
      setLoadingInit(true)
      setInitError(null)
      try {
        const [projs, users] = await Promise.all([
          fetchProjects(),
          fetchAllUsers(),
        ])
        setProjects(projs)
        setKorisnici(users)
      } catch (err) {
        setInitError(err instanceof Error ? err.message : 'Greška pri učitavanju')
      } finally {
        setLoadingInit(false)
      }
    }
    void load()
  }, [])

  async function selectProject(project: Project) {
    setSelectedProject(project)
    setActiveTab('PROJEKAT')
    setProjectDocs([])
    setDocsError(null)
  }

  async function handleTabChange(tab: Tab) {
    setActiveTab(tab)
    if (tab === 'DOKUMENTI' && selectedProject?.id && projectDocs.length === 0) {
      setDocsLoading(true)
      setDocsError(null)
      try {
        const docs = await fetchDocumentsByProject(String(selectedProject.id))
        setProjectDocs(docs)
      } catch (err) {
        setDocsError(err instanceof Error ? err.message : 'Greška pri učitavanju dokumenata')
      } finally {
        setDocsLoading(false)
      }
    }
  }

  return (
    <AppShell>
      <div className="mx-auto flex min-h-[calc(100vh-2rem)] w-full max-w-7xl flex-col gap-4 px-4 py-4 text-ink md:px-6 lg:px-8">
        <div className="flex items-center rounded-lg border border-hairline bg-surface-1 px-4 py-3 shadow-sm">
          <h1 className="m-0 text-2xl font-semibold text-ink">Upravljanje pristupom</h1>
        </div>

        {initError && (
          <div className="rounded-lg border border-error/35 bg-error/10 px-4 py-3 text-sm text-[#ffb4b4]">
            {initError}
          </div>
        )}

        <div className="grid min-h-0 gap-4 lg:grid-cols-[260px_minmax(0,1fr)]">
          {/* Left: projects list */}
          <div className="rounded-lg border border-hairline bg-surface-1">
            <div className="border-b border-hairline px-4 py-2.5 text-xs font-semibold uppercase tracking-wide text-ink-subtle">
              Projekti
            </div>
            {loadingInit ? (
              <div className="px-4 py-3 text-sm text-ink-muted">Učitavanje...</div>
            ) : projects.length === 0 ? (
              <div className="px-4 py-3 text-sm text-ink-muted">Nema projekata.</div>
            ) : (
              <div className="divide-y divide-hairline">
                {projects.map((project) => {
                  const isActive = selectedProject?.id === project.id
                  return (
                    <button
                      key={String(project.id)}
                      type="button"
                      onClick={() => void selectProject(project)}
                      className={`w-full px-4 py-3 text-left text-sm transition-colors hover:bg-surface-2 cursor-pointer ${isActive ? 'bg-surface-2 font-medium text-ink' : 'text-ink-muted'}`}
                    >
                      {project.name}
                    </button>
                  )
                })}
              </div>
            )}
          </div>

          {/* Right: access management */}
          <div className="rounded-lg border border-hairline bg-surface-1">
            {!selectedProject ? (
              <div className="flex h-full items-center justify-center p-8 text-sm text-ink-muted">
                Izaberite projekat
              </div>
            ) : (
              <div className="flex flex-col h-full">
                <div className="border-b border-hairline px-6 py-4">
                  <div className="text-xs font-semibold uppercase tracking-wide text-ink-subtle">Projekat</div>
                  <div className="mt-0.5 text-base font-semibold text-ink">{selectedProject.name}</div>
                </div>

                {/* Tabs */}
                <div className="flex border-b border-hairline px-6">
                  <button
                    type="button"
                    onClick={() => void handleTabChange('PROJEKAT')}
                    className={`-mb-px border-b-2 px-4 py-3 text-sm font-medium transition-colors cursor-pointer ${
                      activeTab === 'PROJEKAT'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-ink-muted hover:text-ink'
                    }`}
                  >
                    Pristup projektu
                  </button>
                  <button
                    type="button"
                    onClick={() => void handleTabChange('DOKUMENTI')}
                    className={`-mb-px border-b-2 px-4 py-3 text-sm font-medium transition-colors cursor-pointer ${
                      activeTab === 'DOKUMENTI'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-ink-muted hover:text-ink'
                    }`}
                  >
                    Dokumenti projekta
                  </button>
                </div>

                <div className="flex-1 overflow-auto p-6">
                  {activeTab === 'PROJEKAT' && (
                    <AccessPanel
                      key={`proj-${String(selectedProject.id)}`}
                      resourceType="PROJEKAT"
                      resourceId={String(selectedProject.id)}
                      korisnici={korisnici}
                      currentUserId={currentUserId}
                    />
                  )}

                  {activeTab === 'DOKUMENTI' && (
                    <div className="flex flex-col gap-3">
                      <p className="text-sm text-ink-muted">
                        Postavite posebna pravila pristupa za pojedinačne dokumente — override projekatnog pristupa.
                      </p>
                      {docsLoading ? (
                        <div className="py-4 text-center text-sm text-ink-muted">Učitavanje dokumenata...</div>
                      ) : docsError ? (
                        <div className="rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
                          {docsError}
                        </div>
                      ) : projectDocs.length === 0 ? (
                        <div className="rounded-md border border-hairline bg-surface-2 px-4 py-3 text-sm text-ink-muted">
                          Nema dokumenata u ovom projektu.
                        </div>
                      ) : (
                        <div className="flex flex-col gap-2">
                          {projectDocs.map((doc) => (
                            <DocumentRow
                              key={doc.id}
                              document={doc}
                              korisnici={korisnici}
                              currentUserId={currentUserId}
                            />
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </AppShell>
  )
}
