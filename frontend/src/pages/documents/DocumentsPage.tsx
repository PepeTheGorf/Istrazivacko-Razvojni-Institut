import { useEffect, useMemo, useState, type Dispatch, type FormEvent, type SetStateAction } from 'react'
import { AdvancedSearchDialog, EMPTY_ADVANCED_FILTERS } from '../../components/AdvancedSearchDialog'
import type { AdvancedFilters } from '../../components/AdvancedSearchDialog'
import { TagDocumentsDialog } from '../../components/TagDocumentsDialog'
import { createDocument, deleteDocument, fetchDocuments, updateDocument, uploadDocument } from '../../api/documents'
import { createDokumentTag, deleteDokumentTag, fetchDocumentTags } from '../../api/documentTags'
import { fetchMetapodatakByDocument } from '../../api/metapodatak'
import { fetchProjectsForSelection } from '../../api/projects'
import { createTag, fetchTags } from '../../api/tags'
import { fetchTipDokumenta } from '../../api/tipDokumenta'
import { fetchTipMetapodataka } from '../../api/tipMetapodatka'
import { useAuth } from '../../auth/AuthContext'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { SelectField } from '../../components/ui/SelectField'
import { TextArea } from '../../components/ui/TextArea'
import { TextInput } from '../../components/ui/TextInput'
import type { Dokument } from '../../types/document'
import type { DokumentTag } from '../../types/documentTag'
import type { Metapodatak } from '../../types/metapodatak'
import type { Project } from '../../types/project'
import type { Tag } from '../../types/tag'
import type { TipDokumenta } from '../../types/tipDokumenta'
import type { TipMetapodatka, TipPodatka } from '../../types/tipMetapodatka'

type DocumentMode = 'create' | 'edit'

interface MetadataRow {
  tipMetapodatkaId: string
  vrednost: string
}

interface DocumentFormValues {
  naslov: string
  authorId: string
  sadrzaj: string
  projektId: string
  tipDokumentaId: string
  tagoviText: string
  metapodaci: MetadataRow[]
}

const EMPTY_VALUES: DocumentFormValues = {
  naslov: '',
  authorId: '',
  sadrzaj: '',
  projektId: '',
  tipDokumentaId: '',
  tagoviText: '',
  metapodaci: [],
}

function createEmptyValues(): DocumentFormValues {
  return { ...EMPTY_VALUES, metapodaci: [] }
}

function splitTags(value: string): string[] {
  return value
    .split(/\s+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function joinTags(tags: string[]): string {
  return tags.join(' ')
}

function mapTagNames(documentTags: DokumentTag[], allTags: Tag[]): string[] {
  const tagNameById = new Map(allTags.map((tag) => [tag.id, tag.naziv]))
  return documentTags.map((item) => tagNameById.get(item.tagId) ?? item.tagId)
}

function formatDate(value?: string) {
  if (!value) return 'N/A'
  return new Date(value).toLocaleDateString('sr-RS')
}

function isCompleteMetadataRow(row: MetadataRow) {
  return Boolean(row.tipMetapodatkaId.trim() || row.vrednost.trim())
}

function metadataOptionsForDocumentType(tipovi: TipMetapodatka[], tipDokumentaId: string) {
  return tipovi.filter((item) => !item.tipDokumentaId || item.tipDokumentaId === tipDokumentaId)
}

function resolveEditableProjectId(document: Dokument, projects: Project[]): string {
  if (document.projectName) {
    const matchByName = projects.find((project) => project.name === document.projectName)
    if (matchByName?.id) {
      return String(matchByName.id)
    }
  }

  if (document.projektId) {
    const matchById = projects.find((project) => String(project.id ?? '') === document.projektId)
    if (matchById?.id) {
      return String(matchById.id)
    }
  }

  return ''
}

function normalizeSearchText(value: string) {
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
}

function valueContains(haystack: string | undefined, needle: string) {
  if (!needle.trim()) return true
  return normalizeSearchText(haystack ?? '').includes(normalizeSearchText(needle))
}

function metadataValueMatches(tipPodatka: TipPodatka | undefined, actual: string, expected: string) {
  const normalizedActual = normalizeSearchText(actual)
  const normalizedExpected = normalizeSearchText(expected)

  if (!normalizedExpected) return true

  if (tipPodatka === 'BROJ') {
    return normalizedActual === normalizedExpected
  }

  if (tipPodatka === 'BOOLEAN') {
    return normalizedActual === normalizedExpected || normalizedActual.startsWith(normalizedExpected)
  }

  return normalizedActual.includes(normalizedExpected)
}

export function DocumentsPage() {
  const { user } = useAuth()
  const [documents, setDocuments] = useState<Dokument[]>([])
  const [documentTagNamesByDocumentId, setDocumentTagNamesByDocumentId] = useState<Map<string, string[]>>(new Map())
  const [documentMetadataByDocumentId, setDocumentMetadataByDocumentId] = useState<Map<string, Metapodatak[]>>(new Map())
  const [selectedDocumentId, setSelectedDocumentId] = useState<string | null>(null)
  const [selectedDocumentMetadata, setSelectedDocumentMetadata] = useState<Metapodatak[]>([])
  const [selectedDocumentMetadataLoading, setSelectedDocumentMetadataLoading] = useState(false)
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [advancedSearchOpen, setAdvancedSearchOpen] = useState(false)
  const [advancedFilters, setAdvancedFilters] = useState<AdvancedFilters>(EMPTY_ADVANCED_FILTERS)
  const [projects, setProjects] = useState<Project[]>([])
  const [tags, setTags] = useState<Tag[]>([])
  const [tipoviDokumenta, setTipoviDokumenta] = useState<TipDokumenta[]>([])
  const [tipoviMetapodataka, setTipoviMetapodataka] = useState<TipMetapodatka[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [tagDialogOpen, setTagDialogOpen] = useState(false)
  const [tagInput, setTagInput] = useState('')
  const [tagSaving, setTagSaving] = useState(false)
  const [tagError, setTagError] = useState<string | null>(null)

  const [dialogOpen, setDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<DocumentMode>('create')
  const [dialogLoading, setDialogLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [activeDocument, setActiveDocument] = useState<Dokument | null>(null)
  const [formValues, setFormValues] = useState<DocumentFormValues>(createEmptyValues())
  const [formError, setFormError] = useState<string | null>(null)

  const [uploadDialogOpen, setUploadDialogOpen] = useState(false)
  const [uploadFile, setUploadFile] = useState<File | null>(null)
  const [uploadNaziv, setUploadNaziv] = useState('')
  const [uploadTipDokumentaId, setUploadTipDokumentaId] = useState('')
  const [uploadProjektId, setUploadProjektId] = useState('')
  const [uploadMetapodaci, setUploadMetapodaci] = useState<MetadataRow[]>([])
  const [uploadSubmitting, setUploadSubmitting] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)

  const projectNameById = useMemo(
    () => new Map(projects.filter((project) => project.id).map((project) => [project.id as string, project.name])),
    [projects],
  )

  const tagNameById = useMemo(
    () => new Map(tags.map((tag) => [tag.id, tag.naziv])),
    [tags],
  )

  const tipDokumentaNameById = useMemo(
    () => new Map(tipoviDokumenta.map((item) => [item.id, item.naziv])),
    [tipoviDokumenta],
  )

  const tipMetapodatkaNameById = useMemo(
    () => new Map(tipoviMetapodataka.map((item) => [item.id, item.naziv])),
    [tipoviMetapodataka],
  )

  const tipMetapodatkaById = useMemo(
    () => new Map(tipoviMetapodataka.map((item) => [item.id, item])),
    [tipoviMetapodataka],
  )

  const documentsById = useMemo(
    () => new Map(documents.map((document) => [document.id, document])),
    [documents],
  )

  const selectedMetadataOptions = useMemo(
    () => metadataOptionsForDocumentType(tipoviMetapodataka, formValues.tipDokumentaId),
    [tipoviMetapodataka, formValues.tipDokumentaId],
  )

  const uploadRequiredMetadata = useMemo(
    () => tipoviMetapodataka.filter(
      (item) => item.jeObavezan && (!item.tipDokumentaId || item.tipDokumentaId === uploadTipDokumentaId),
    ),
    [tipoviMetapodataka, uploadTipDokumentaId],
  )

  const selectedDocument = useMemo(
    () => documents.find((document) => document.id === selectedDocumentId) ?? null,
    [documents, selectedDocumentId],
  )

  const filteredDocuments = useMemo(() => {
    const q = normalizeSearchText(searchQuery)
    const af = advancedFilters

    return documents.filter((document) => {
      const documentMetadata = documentMetadataByDocumentId.get(document.id) ?? []
      const tagNames = documentTagNamesByDocumentId.get(document.id) ?? []
      const documentProjectName = document.projectName ?? projectNameById.get(document.projektId ?? '') ?? ''

      // sidebar project filter
      if (selectedProjectId) {
        const selectedProject = projects.find((p) => p.id === selectedProjectId)
        const matchesProject =
          document.projektId === selectedProjectId ||
          (selectedProject?.name ? documentProjectName === selectedProject.name : false)
        if (!matchesProject) return false
      }

      // main search bar — matches title, tags, metadata values
      if (q) {
        const searchableText = [
          document.naslov,
          ...tagNames,
          ...documentMetadata.map((item) => item.vrednost),
        ].join(' ')
        if (!valueContains(searchableText, q)) return false
      }

      // advanced filters
      if (af.title.trim() && !valueContains(document.naslov, af.title)) return false
      if (af.author.trim()) {
        const authorCandidate = `${document.authorName ?? ''} ${document.authorId}`
        if (!valueContains(authorCandidate, af.author)) return false
      }
      if (af.tipDokumentaId && document.tipDokumentaId !== af.tipDokumentaId) return false
      if (af.tag.trim()) {
        if (!tagNames.some((n) => valueContains(n, af.tag))) return false
      }
      if (af.dateFrom) {
        const docDate = document.createdAt ? new Date(document.createdAt) : null
        if (!docDate || docDate < new Date(af.dateFrom)) return false
      }
      if (af.dateTo) {
        const docDate = document.createdAt ? new Date(document.createdAt) : null
        if (!docDate || docDate > new Date(af.dateTo + 'T23:59:59')) return false
      }
      if (af.projektId) {
        const selectedProject = projects.find((p) => p.id === af.projektId)
        const matchesProject =
          document.projektId === af.projektId ||
          (selectedProject?.name ? documentProjectName === selectedProject.name : false)
        if (!matchesProject) return false
      }
      // advanced metadata filters
      for (const row of af.metadataFilters) {
        if (!row.tipMetapodatkaId && !row.vrednost.trim()) continue
        const matchingMetadata = documentMetadata.filter((item) => item.tipMetapodatkaId === row.tipMetapodatkaId)
        if (!matchingMetadata.length) return false
        const metadataType = tipMetapodatkaById.get(row.tipMetapodatkaId)
        if (!matchingMetadata.some((item) => metadataValueMatches(metadataType?.tipPodatka, item.vrednost, row.vrednost))) {
          return false
        }
      }

      return true
    })
  }, [
    advancedFilters,
    documentMetadataByDocumentId,
    documentTagNamesByDocumentId,
    documents,
    projects,
    projectNameById,
    searchQuery,
    selectedProjectId,
    tipMetapodatkaById,
  ])

  const currentAuthorLabel = user ? `${user.name} ${user.surname}` : 'Nijedan korisnik nije prijavljen'

  async function loadAll() {
    setLoading(true)
    setError(null)
    try {
      const [documentsData, projectsData, tagsData, tipoviData, tipoviMetapodatakaData] = await Promise.all([
        fetchDocuments(),
        fetchProjectsForSelection(),
        fetchTags(),
        fetchTipDokumenta(),
        fetchTipMetapodataka(),
      ])

      const tagNameByIdLocal = new Map(tagsData.map((tag) => [tag.id, tag.naziv]))
      const tagResults = await Promise.all(
        documentsData.map(async (document) => {
          try {
            const documentTags = await fetchDocumentTags(document.id)
            const resolvedNames = documentTags.map((item) => tagNameByIdLocal.get(item.tagId) ?? item.tagId)
            return [document.id, resolvedNames] as const
          } catch {
            return [document.id, [] as string[]] as const
          }
        }),
      )

      const metadataResults = await Promise.all(
        documentsData.map(async (document) => {
          try {
            const documentMetadata = await fetchMetapodatakByDocument(document.id)
            return [document.id, documentMetadata] as const
          } catch {
            return [document.id, [] as Metapodatak[]] as const
          }
        }),
      )

      setDocuments(documentsData)
      setDocumentTagNamesByDocumentId(new Map(tagResults))
      setDocumentMetadataByDocumentId(new Map(metadataResults))
      setProjects(projectsData)
      setTags(tagsData)
      setTipoviDokumenta(tipoviData)
      setTipoviMetapodataka(tipoviMetapodatakaData)
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : 'Neuspesno ucitavanje dokumenata')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadAll()
  }, [])


  useEffect(() => {
    if (!selectedDocumentId) {
      setSelectedDocumentMetadata([])
      return
    }

    const exists = filteredDocuments.some((document) => document.id === selectedDocumentId)
    if (!exists) {
      setSelectedDocumentId(null)
      setSelectedDocumentMetadata([])
    }
  }, [filteredDocuments, selectedDocumentId])

  async function selectDocument(document: Dokument) {
    setSelectedDocumentId(document.id)
    setSelectedDocumentMetadataLoading(true)
    try {
      const metadata = documentMetadataByDocumentId.get(document.id)
      if (metadata) {
        setSelectedDocumentMetadata(metadata)
      } else {
        const fetchedMetadata = await fetchMetapodatakByDocument(document.id)
        setSelectedDocumentMetadata(fetchedMetadata)
      }
    } catch {
      setSelectedDocumentMetadata([])
    } finally {
      setSelectedDocumentMetadataLoading(false)
    }
  }

  function closeDocumentDetails() {
    setSelectedDocumentId(null)
    setSelectedDocumentMetadata([])
    setSelectedDocumentMetadataLoading(false)
    setTagInput('')
    setTagError(null)
  }

  function openCreateDialog() {
    setDialogMode('create')
    setActiveDocument(null)
    setFormValues({
      ...createEmptyValues(),
      authorId: user ? String(user.id) : '',
    })
    setFormError(null)
    setDialogLoading(false)
    setDialogOpen(true)
  }

  async function openEditDialog(document: Dokument) {
    setDialogMode('edit')
    setActiveDocument(document)
    setDialogOpen(true)
    setDialogLoading(true)
    setFormError(null)

    try {
      const [documentTags, documentMetadata] = await Promise.all([
        fetchDocumentTags(document.id),
        fetchMetapodatakByDocument(document.id),
      ])

      setFormValues({
        naslov: document.naslov ?? '',
        authorId: user ? String(user.id) : document.authorId ?? '',
        sadrzaj: document.sadrzaj ?? '',
        projektId: resolveEditableProjectId(document, projects),
        tipDokumentaId: document.tipDokumentaId ?? '',
        tagoviText: joinTags(mapTagNames(documentTags, tags)),
        metapodaci: documentMetadata.map((item: Metapodatak) => ({
          tipMetapodatkaId: item.tipMetapodatkaId,
          vrednost: item.vrednost,
        })),
      })
    } catch (caughtError) {
      setFormError(caughtError instanceof Error ? caughtError.message : 'Neuspesno ucitavanje dokumenta')
    } finally {
      setDialogLoading(false)
    }
  }

  async function handleDelete(document: Dokument) {
    if (!confirm(`Obrisi dokument \"${document.naslov}\"?`)) return
    await deleteDocument(document.id)
    await loadAll()
  }

  function closeDialog() {
    setDialogOpen(false)
    setFormError(null)
    setDialogLoading(false)
    setSubmitting(false)
    setActiveDocument(null)
    setFormValues(createEmptyValues())
  }

  async function addTagToDocument(tagName: string) {
    if (!selectedDocumentId || !tagName.trim()) return
    setTagSaving(true)
    setTagError(null)
    try {
      const trimmed = tagName.trim()
      let tag = tags.find((t) => t.naziv.toLowerCase() === trimmed.toLowerCase())
      if (!tag) {
        tag = await createTag(trimmed)
        setTags((prev) => [...prev, tag!])
      }
      await createDokumentTag(selectedDocumentId, tag.id)
      const updatedTagLinks = await fetchDocumentTags(selectedDocumentId)
      const freshTag = tag
      setTags((latestTags) => {
        const allTags = latestTags.find((t) => t.id === freshTag.id)
          ? latestTags
          : [...latestTags, freshTag]
        const resolvedNames = updatedTagLinks.map(
          (link) => allTags.find((t) => t.id === link.tagId)?.naziv ?? link.tagId,
        )
        setDocumentTagNamesByDocumentId((prev) => new Map(prev).set(selectedDocumentId, resolvedNames))
        return allTags
      })
      setTagInput('')
    } catch (err) {
      setTagError(err instanceof Error ? err.message : 'Greška pri dodavanju taga')
    } finally {
      setTagSaving(false)
    }
  }

  async function removeTagFromDocument(tagName: string) {
    if (!selectedDocumentId) return
    setTagError(null)
    try {
      // tagName may be a raw UUID when the tag wasn't in the fetched tags list
      const tag = tags.find((t) => t.naziv === tagName) ?? tags.find((t) => t.id === tagName)
      const tagId = tag?.id ?? (tagName.match(/^[0-9a-f-]{36}$/i) ? tagName : null)
      if (!tagId) return
      await deleteDokumentTag(selectedDocumentId, tagId)
      setDocumentTagNamesByDocumentId((prev) => {
        const updated = new Map(prev)
        updated.set(selectedDocumentId, (prev.get(selectedDocumentId) ?? []).filter((n) => n !== tagName))
        return updated
      })
    } catch (err) {
      setTagError(err instanceof Error ? err.message : 'Greška pri uklanjanju taga')
    }
  }

  function closeUploadDialog() {
    setUploadDialogOpen(false)
    setUploadFile(null)
    setUploadNaziv('')
    setUploadTipDokumentaId('')
    setUploadProjektId('')
    setUploadMetapodaci([])
    setUploadError(null)
    setUploadSubmitting(false)
  }

  function handleUploadTipChange(tipId: string) {
    setUploadTipDokumentaId(tipId)
    const required = tipoviMetapodataka.filter(
      (item) => item.jeObavezan && (!item.tipDokumentaId || item.tipDokumentaId === tipId),
    )
    setUploadMetapodaci(required.map((item) => ({ tipMetapodatkaId: item.id, vrednost: '' })))
  }

  async function handleUploadSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!uploadFile) {
      setUploadError('Izaberite fajl.')
      return
    }
    if (!uploadProjektId) {
      setUploadError('Projekat je obavezan.')
      return
    }
    if (!uploadTipDokumentaId) {
      setUploadError('Tip dokumenta je obavezan.')
      return
    }
    const unfilled = uploadMetapodaci.filter((row) => !row.vrednost.trim())
    if (unfilled.length > 0) {
      setUploadError('Svi obavezni metapodaci moraju biti popunjeni.')
      return
    }
    setUploadSubmitting(true)
    setUploadError(null)
    try {
      await uploadDocument({
        file: uploadFile,
        naziv: uploadNaziv || undefined,
        tipDokumentaId: uploadTipDokumentaId,
        projektId: uploadProjektId,
        projectName: projects.find((p) => p.id === uploadProjektId)?.name,
        authorId: user ? String(user.id) : undefined,
        authorName: currentAuthorLabel,
        metapodaci: uploadMetapodaci.filter((row) => row.vrednost.trim()),
      })
      closeUploadDialog()
      await loadAll()
    } catch (err) {
      setUploadError(err instanceof Error ? err.message : 'Greška pri otpremanju dokumenta')
    } finally {
      setUploadSubmitting(false)
    }
  }

  function clearAllFilters() {
    setSearchQuery('')
    setSelectedProjectId(null)
    setAdvancedFilters(EMPTY_ADVANCED_FILTERS)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const resolvedAuthorId = formValues.authorId.trim() || (user ? String(user.id) : '')

    if (!formValues.naslov.trim()) {
      setFormError('Naziv je obavezan.')
      return
    }

    if (!resolvedAuthorId) {
      setFormError('Autor je obavezan.')
      return
    }

    if (!formValues.projektId.trim()) {
      setFormError('Projekat je obavezan.')
      return
    }

    if (!formValues.tipDokumentaId.trim()) {
      setFormError('Tip dokumenta je obavezan.')
      return
    }

    const invalidMetadata = formValues.metapodaci.some(
      (row) => isCompleteMetadataRow(row) && (!row.tipMetapodatkaId.trim() || !row.vrednost.trim()),
    )

    if (invalidMetadata) {
      setFormError('Svaki red metapodataka mora imati i tip i vrednost.')
      return
    }

    const payload = {
      naslov: formValues.naslov.trim(),
      authorId: resolvedAuthorId,
      authorName: currentAuthorLabel,
      sadrzaj: formValues.sadrzaj.trim(),
      projektId: formValues.projektId.trim(),
      projectName: projects.find((project) => project.id === formValues.projektId)?.name ?? null,
      tipDokumentaId: formValues.tipDokumentaId.trim(),
      tagovi: splitTags(formValues.tagoviText),
      metapodaci: formValues.metapodaci
        .filter((row) => isCompleteMetadataRow(row))
        .map((row) => ({
          tipMetapodatkaId: row.tipMetapodatkaId.trim(),
          vrednost: row.vrednost.trim(),
        })),
    }

    setSubmitting(true)
    setFormError(null)

    try {
      if (dialogMode === 'edit' && activeDocument) {
        await updateDocument(activeDocument.id, payload)
      } else {
        await createDocument(payload)
      }
      closeDialog()
      await loadAll()
    } catch (caughtError) {
      setFormError(caughtError instanceof Error ? caughtError.message : 'Neuspesno cuvanje dokumenta')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AppShell>
      <div className="mx-auto flex min-h-[calc(100vh-2rem)] w-full max-w-7xl flex-col gap-4 px-4 py-4 text-ink md:px-6 lg:px-8">
        <div className="flex items-center justify-between rounded-lg border border-hairline bg-surface-1 px-4 py-3 shadow-sm">
          <div>
            <h1 className="m-0 text-2xl font-semibold text-ink">Dokumenti</h1>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="secondary" onClick={() => setUploadDialogOpen(true)}>
              Otpremi dokument
            </Button>
            <Button icon="add" onClick={openCreateDialog}>
              Novi dokument
            </Button>
          </div>
        </div>

        {error ? (
          <div className="rounded-lg border border-error/35 bg-error/10 px-4 py-3 text-sm text-[#ffb4b4]">
            {error}
          </div>
        ) : null}

        <div className="flex items-center gap-2">
          <div className="flex flex-1 items-center gap-2 rounded-lg border border-hairline bg-surface-1 px-3 py-2 shadow-sm">
            <span className="flex shrink-0 items-center justify-center text-ink-muted">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden>
                <circle cx="6.5" cy="6.5" r="4.5" stroke="currentColor" strokeWidth="1.5" />
                <path d="M10.5 10.5L14 14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
              </svg>
            </span>
            <input
              type="text"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Search..."
              className="flex-1 bg-transparent py-1 text-sm text-ink placeholder:text-ink-tertiary focus:outline-none"
            />
          </div>
          <button
            type="button"
            onClick={() => setAdvancedSearchOpen(true)}
            title="Advanced search"
            className={`flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-lg border border-hairline shadow-sm transition-colors hover:bg-surface-2 ${Object.values(advancedFilters).some(Boolean) ? 'bg-primary/15 text-primary border-primary/40' : 'bg-surface-1 text-ink-muted'}`}
            aria-label="Advanced search"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
              <path d="M4 6h16M7 12h10M10 18h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" fill="none" />
            </svg>
          </button>
          <button
            type="button"
            onClick={() => setTagDialogOpen(true)}
            title="Tag documents"
            className="flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-lg border border-hairline bg-surface-1 shadow-sm text-ink-muted transition-colors hover:bg-surface-2 hover:text-ink"
            aria-label="Tag documents"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
              <path d="M21.41 11.58l-9-9A2 2 0 0011 2H4a2 2 0 00-2 2v7a2 2 0 00.59 1.42l9 9a2 2 0 002.82 0l7-7a2 2 0 000-2.84zM7 9a2 2 0 110-4 2 2 0 010 4z" />
            </svg>
          </button>
        </div>

        <div className="grid min-h-0 gap-4 lg:grid-cols-[280px_minmax(0,1fr)]">
          <aside className="rounded-lg border border-hairline bg-surface-1 p-4 lg:sticky lg:top-4 lg:self-start lg:max-h-[calc(100vh-6rem)] lg:overflow-auto">
            <div className="mb-3 text-sm font-semibold uppercase tracking-wide text-ink-muted">
              Projekti
            </div>
            <div className="space-y-2">
              <button
                type="button"
                onClick={() => setSelectedProjectId(null)}
                className={`w-full rounded-md border px-3 py-2 text-left text-sm ${selectedProjectId === null
                  ? 'border-hairline bg-surface-2 text-ink'
                  : 'border-hairline bg-surface-1 text-ink-muted hover:bg-surface-2'
                  }`}
              >
                Svi projekti
              </button>
              {projects.map((project) => (
                <button
                  type="button"
                  onClick={() => setSelectedProjectId(project.id ?? null)}
                  key={project.id ?? project.name}
                  className={`w-full rounded-md border px-3 py-2 text-left text-sm ${selectedProjectId === (project.id ?? null)
                    ? 'border-hairline bg-surface-2 text-ink'
                    : 'border-hairline bg-surface-1 text-ink-muted hover:bg-surface-2'
                    }`}
                >
                  <div className="font-medium">{project.name}</div>
                  {project.description ? (
                    <div className="mt-1 line-clamp-2 text-xs text-ink-subtle">{project.description}</div>
                  ) : null}
                </button>
              ))}
              {!projects.length && !loading ? <div className="text-sm text-ink-subtle">Nema projekata.</div> : null}
            </div>
          </aside>

          <div className="min-h-0 rounded-lg border border-hairline bg-surface-1">
            <div className="border-b border-hairline px-4 py-3">
              <div className="grid grid-cols-[minmax(0,2fr)_minmax(0,1fr)_160px_112px] gap-4 text-sm font-medium text-ink-subtle">
                <div>Naziv</div>
                <div>Autor</div>
                <div>Datum</div>
                <div className="text-right">Akcije</div>
              </div>
            </div>

            <div className="divide-y divide-hairline">
              {loading ? (
                <div className="px-4 py-10 text-sm text-ink-subtle">Ucitavanje dokumenata...</div>
              ) : filteredDocuments.length ? (
                filteredDocuments.map((document) => {
                  const isSelected = selectedDocumentId === document.id
                  return (
                    <div
                      key={document.id}
                      role="button"
                      tabIndex={0}
                      onClick={() => void selectDocument(document)}
                      onKeyDown={(event) => {
                        if (event.key === 'Enter' || event.key === ' ') {
                          event.preventDefault()
                          void selectDocument(document)
                        }
                      }}
                      className={`grid grid-cols-[minmax(0,2fr)_minmax(0,1fr)_160px_112px] items-start gap-4 px-4 py-4 hover:bg-surface-2/70 cursor-pointer ${isSelected ? 'bg-surface-2/70' : ''}`}
                    >
                      <div className="min-w-0">
                        <div className="truncate text-base font-medium text-ink">{document.naslov}</div>
                        <div className="mt-2 flex flex-wrap gap-2">
                          {(() => {
                            const tagNames = documentTagNamesByDocumentId.get(document.id) ?? []
                            if (!tagNames.length) {
                              return null
                            }

                            return tagNames.map((tagName) => (
                              <span key={`${document.id}-${tagName}`} className="rounded-full bg-surface-2 px-2 py-1 text-xs text-ink-subtle">
                                {tagName}
                              </span>
                            ))
                          })()}
                        </div>
                      </div>
                      <div className="truncate text-sm text-ink-muted">{document.authorName ?? document.authorId}</div>
                      <div className="text-sm text-ink-muted">{formatDate(document.createdAt)}</div>
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="secondary"
                          onClick={(event) => {
                            event.stopPropagation()
                            void openEditDialog(document)
                          }}
                        >
                          Izmeni
                        </Button>
                        <Button
                          variant="delete"
                          onClick={(event) => {
                            event.stopPropagation()
                            void handleDelete(document)
                          }}
                        >
                          Obrisi
                        </Button>
                      </div>
                    </div>
                  )
                })
              ) : (
                <div className="px-4 py-10 text-sm text-ink-subtle">
                  {searchQuery.trim() || Object.values(advancedFilters).some(Boolean) || selectedProjectId
                    ? 'Nema dokumenata za izabrane filtere.'
                    : 'Nema dokumenata.'}
                </div>
              )}
            </div>
          </div>
        </div>

        <div
          className={`fixed inset-0 z-40 bg-black/45 transition-opacity duration-200 ${selectedDocument ? 'opacity-100' : 'pointer-events-none opacity-0'}`}
          onClick={closeDocumentDetails}
          aria-hidden="true"
        />

        <aside
          className={`fixed right-0 top-0 z-50 h-full w-full max-w-md border-l border-hairline bg-surface-1 shadow-2xl transition-transform duration-300 ${selectedDocument ? 'translate-x-0' : 'translate-x-full'}`}
          aria-label="Detalji dokumenta"
        >
          <div className="flex h-full flex-col">
            <div className="flex items-center justify-between border-b border-hairline px-4 py-3">
              <div className="text-sm font-semibold uppercase tracking-wide text-ink-muted">Detalji dokumenta</div>
              <button
                type="button"
                className="rounded-md border border-hairline px-2 py-1 text-sm text-ink-muted hover:bg-surface-2"
                onClick={closeDocumentDetails}
                aria-label="Zatvori detalje"
              >
                X
              </button>
            </div>

            <div className="min-h-0 flex-1 overflow-auto p-4">
              {!selectedDocument ? (
                <div className="rounded-md border border-dashed border-hairline px-3 py-4 text-sm text-ink-subtle">
                  Klikni na dokument da se prikažu informacije i metapodaci.
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="rounded-md border border-hairline bg-surface-2 px-3 py-3">
                    <div className="text-base font-semibold text-ink">{selectedDocument.naslov}</div>
                    <div className="mt-2 text-xs text-ink-subtle">Autor: {selectedDocument.authorName ?? selectedDocument.authorId}</div>
                    <div className="mt-1 text-xs text-ink-subtle">
                      Projekat: {selectedDocument.projectName ?? projectNameById.get(selectedDocument.projektId ?? '') ?? selectedDocument.projektId ?? 'N/A'}
                    </div>
                    <div className="mt-1 text-xs text-ink-subtle">
                      Tip: {tipDokumentaNameById.get(selectedDocument.tipDokumentaId ?? '') ?? selectedDocument.tipDokumentaId ?? 'N/A'}
                    </div>
                    <div className="mt-1 text-xs text-ink-subtle">Kreirano: {formatDate(selectedDocument.createdAt)}</div>
                  </div>

                  <div className="rounded-md border border-hairline bg-surface-2 px-3 py-3">
                    <div className="text-xs font-medium uppercase tracking-wide text-ink-subtle">Tagovi</div>
                    <div className="mt-2 flex flex-wrap gap-1.5">
                      {(documentTagNamesByDocumentId.get(selectedDocument.id) ?? []).length ? (
                        (documentTagNamesByDocumentId.get(selectedDocument.id) ?? []).map((tagName) => (
                          <span
                            key={`panel-${selectedDocument.id}-${tagName}`}
                            className="group flex items-center gap-1 rounded-full bg-surface-1 px-2.5 py-1 text-xs text-ink-subtle"
                          >
                            {tagName}
                            <button
                              type="button"
                              onClick={() => void removeTagFromDocument(tagName)}
                              className="ml-0.5 cursor-pointer text-ink-tertiary opacity-0 transition-opacity hover:text-error group-hover:opacity-100"
                              aria-label={`Ukloni tag ${tagName}`}
                            >
                              ×
                            </button>
                          </span>
                        ))
                      ) : (
                        <span className="text-sm text-ink-subtle">Nema tagova</span>
                      )}
                    </div>
                    <div className="mt-3 flex items-center gap-1.5">
                      <input
                        type="text"
                        value={tagInput}
                        onChange={(e) => setTagInput(e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') void addTagToDocument(tagInput)
                          if (e.key === 'Escape') setTagInput('')
                        }}
                        placeholder="Tag..."
                        disabled={tagSaving}
                        className="min-w-0 flex-1 rounded-md border border-hairline bg-surface-1 px-2.5 py-1.5 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50 disabled:opacity-50"
                      />
                      <button
                        type="button"
                        onClick={() => void addTagToDocument(tagInput)}
                        disabled={tagSaving || !tagInput.trim()}
                        className="flex h-8 w-8 cursor-pointer items-center justify-center rounded-md border border-hairline bg-surface-1 text-ink-muted transition-colors hover:border-primary/50 hover:text-primary disabled:cursor-not-allowed disabled:opacity-40"
                        aria-label="Dodaj tag"
                      >
                        {tagSaving ? (
                          <svg className="h-3.5 w-3.5 animate-spin" viewBox="0 0 24 24" fill="none">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
                          </svg>
                        ) : (
                          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden>
                            <path d="M2 7l3.5 3.5L12 3" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" />
                          </svg>
                        )}
                      </button>
                      <button
                        type="button"
                        onClick={() => { setTagInput(''); setTagError(null) }}
                        disabled={tagSaving}
                        className="flex h-8 w-8 cursor-pointer items-center justify-center rounded-md border border-hairline bg-surface-1 text-ink-muted transition-colors hover:border-error/50 hover:text-error disabled:cursor-not-allowed disabled:opacity-40"
                        aria-label="Otkaži"
                      >
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden>
                          <path d="M10 2L2 10M2 2l8 8" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" />
                        </svg>
                      </button>
                    </div>
                    {tagError && (
                      <div className="mt-1.5 text-xs text-error">{tagError}</div>
                    )}
                  </div>

                  <div className="rounded-md border border-hairline bg-surface-2 px-3 py-3">
                    <div className="text-xs font-medium uppercase tracking-wide text-ink-subtle">Sadrzaj</div>
                    <div className="mt-2 whitespace-pre-wrap break-words text-sm text-ink-muted">
                      {selectedDocument.sadrzaj?.trim() ? selectedDocument.sadrzaj : 'Nema sadrzaja.'}
                    </div>
                  </div>

                  <div className="rounded-md border border-hairline bg-surface-2 px-3 py-3">
                    <div className="text-xs font-medium uppercase tracking-wide text-ink-subtle">Metapodaci</div>
                    {selectedDocumentMetadataLoading ? (
                      <div className="mt-2 text-sm text-ink-subtle">Ucitavanje metapodataka...</div>
                    ) : selectedDocumentMetadata.length ? (
                      <div className="mt-2 space-y-2">
                        {selectedDocumentMetadata.map((item) => (
                          <div key={item.id} className="rounded border border-hairline bg-surface-1 px-2 py-2">
                            <div className="text-xs font-medium text-ink">
                              {tipMetapodatkaNameById.get(item.tipMetapodatkaId) ?? item.tipMetapodatkaId}
                            </div>
                            <div className="mt-1 break-words text-xs text-ink-subtle">{item.vrednost}</div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="mt-2 text-sm text-ink-subtle">Nema metapodataka.</div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </aside>

        {uploadDialogOpen ? (
          <div className="fixed inset-0 z-40 overflow-y-auto bg-black/65 p-4">
            <div className="mx-auto my-16 w-full max-w-md rounded-lg border border-hairline bg-surface-1 shadow-2xl">
              <div className="flex items-center justify-between border-b border-hairline px-4 py-3">
                <h2 className="m-0 text-xl font-semibold text-ink">Otpremi dokument</h2>
                <Button variant="secondary" onClick={closeUploadDialog} disabled={uploadSubmitting}>
                  Zatvori
                </Button>
              </div>
              <form className="grid gap-4 p-4" onSubmit={(e) => void handleUploadSubmit(e)}>
                {uploadError ? (
                  <div className="rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
                    {uploadError}
                  </div>
                ) : null}

                <div className="grid gap-1">
                  <label className="text-[13px] font-medium text-ink-muted">Fajl *</label>
                  <input
                    type="file"
                    accept=".pdf,.docx,.txt"
                    disabled={uploadSubmitting}
                    onChange={(e) => setUploadFile(e.target.files?.[0] ?? null)}
                    className="rounded-md border border-hairline bg-surface-1 px-3 py-2 text-sm text-ink file:mr-3 file:cursor-pointer file:rounded file:border-0 file:bg-surface-2 file:px-3 file:py-1 file:text-sm file:text-ink-muted hover:file:bg-surface-2/80 disabled:opacity-50"
                  />
                </div>

                <TextInput
                  label="Naziv dokumenta"
                  name="uploadNaziv"
                  disabled={uploadSubmitting}
                  placeholder="Ostavi prazno da se koristi ime fajla"
                  value={uploadNaziv}
                  onChange={(e) => setUploadNaziv(e.target.value)}
                />

                <SelectField
                  label="Projekat *"
                  name="uploadProjektId"
                  disabled={uploadSubmitting}
                  value={uploadProjektId}
                  onChange={(e) => setUploadProjektId(e.target.value)}
                >
                  <option value="">Izaberi projekat</option>
                  {projects.map((project) => (
                    <option key={project.id ?? project.name} value={project.id ?? ''}>
                      {project.name}
                    </option>
                  ))}
                </SelectField>

                <SelectField
                  label="Tip dokumenta *"
                  name="uploadTipDokumentaId"
                  disabled={uploadSubmitting}
                  value={uploadTipDokumentaId}
                  onChange={(e) => handleUploadTipChange(e.target.value)}
                  required
                >
                  <option value="">Izaberi tip dokumenta</option>
                  {tipoviDokumenta.map((type) => (
                    <option key={type.id} value={type.id}>
                      {type.naziv}
                    </option>
                  ))}
                </SelectField>

                {uploadRequiredMetadata.length > 0 ? (
                  <div className="grid gap-3 rounded-lg border border-hairline bg-surface-2 p-3">
                    <div className="text-xs font-semibold uppercase tracking-wide text-ink-muted">
                      Obavezni metapodaci
                    </div>
                    {uploadRequiredMetadata.map((tip, index) => (
                      <TextInput
                        key={tip.id}
                        label={`${tip.naziv} (${tip.tipPodatka}) *`}
                        name={`uploadMeta-${tip.id}`}
                        disabled={uploadSubmitting}
                        value={uploadMetapodaci[index]?.vrednost ?? ''}
                        onChange={(e) =>
                          setUploadMetapodaci((prev) =>
                            prev.map((row, i) => (i === index ? { ...row, vrednost: e.target.value } : row)),
                          )
                        }
                      />
                    ))}
                  </div>
                ) : null}

                <div className="flex items-center justify-end gap-3 border-t border-hairline pt-4">
                  <Button type="button" variant="secondary" onClick={closeUploadDialog} disabled={uploadSubmitting}>
                    Otkazi
                  </Button>
                  <Button type="submit" disabled={uploadSubmitting || !uploadFile}>
                    {uploadSubmitting ? 'Otpremanje...' : 'Otpremi'}
                  </Button>
                </div>
              </form>
            </div>
          </div>
        ) : null}

        <DocumentDialog
          open={dialogOpen}
          mode={dialogMode}
          loading={dialogLoading}
          submitting={submitting}
          formError={formError}
          values={formValues}
          projects={projects}
          tipoviDokumenta={tipoviDokumenta}
          selectedMetadataOptions={selectedMetadataOptions}
          authorLabel={currentAuthorLabel}
          onClose={closeDialog}
          onSubmit={handleSubmit}
          onChange={setFormValues}
          onMetadataFieldChange={(tipMetapodatkaId, vrednost) =>
            setFormValues((previous) => {
              const exists = previous.metapodaci.some((r) => r.tipMetapodatkaId === tipMetapodatkaId)
              return {
                ...previous,
                metapodaci: exists
                  ? previous.metapodaci.map((r) => r.tipMetapodatkaId === tipMetapodatkaId ? { ...r, vrednost } : r)
                  : [...previous.metapodaci, { tipMetapodatkaId, vrednost }],
              }
            })
          }
        />

        <TagDocumentsDialog
          isOpen={tagDialogOpen}
          onClose={() => setTagDialogOpen(false)}
          onApplied={() => void loadAll()}
        />

        <AdvancedSearchDialog
          isOpen={advancedSearchOpen}
          filters={advancedFilters}
          projects={projects}
          tipoviDokumenta={tipoviDokumenta}
          tipoviMetapodataka={tipoviMetapodataka}
          onApply={(f) => { setAdvancedFilters(f); setAdvancedSearchOpen(false) }}
          onClose={() => setAdvancedSearchOpen(false)}
        />
      </div>
    </AppShell>
  )
}

interface DocumentDialogProps {
  open: boolean
  mode: DocumentMode
  loading: boolean
  submitting: boolean
  formError: string | null
  values: DocumentFormValues
  projects: Project[]
  tipoviDokumenta: TipDokumenta[]
  selectedMetadataOptions: TipMetapodatka[]
  authorLabel: string
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: Dispatch<SetStateAction<DocumentFormValues>>
  onMetadataFieldChange: (tipMetapodatkaId: string, vrednost: string) => void
}

function DocumentDialog({
  open,
  mode,
  loading,
  submitting,
  formError,
  values,
  projects,
  tipoviDokumenta,
  selectedMetadataOptions,
  authorLabel,
  onClose,
  onSubmit,
  onChange,
  onMetadataFieldChange,
}: DocumentDialogProps) {
  if (!open) return null

  const title = mode === 'create' ? 'Dodaj dokument' : 'Izmeni dokument'
  const tagTokens = splitTags(values.tagoviText)

  return (
    <div className="fixed inset-0 z-40 overflow-y-auto bg-black/65 p-4">
      <div className="mx-auto my-6 w-full max-w-6xl rounded-lg border border-hairline bg-surface-1 shadow-2xl">
        <div className="flex items-center justify-between border-b border-hairline px-4 py-3 md:px-6">
          <div>
            <h2 className="m-0 text-xl font-semibold text-ink">{title}</h2>
            <p className="m-0 mt-1 text-sm text-ink-subtle">
              Dokument mora da pripada projektu, tagovi se pišu kao tekst odvojen razmacima, a metapodaci se biraju po tipu dokumenta.
            </p>
          </div>
          <Button variant="secondary" onClick={onClose} disabled={submitting}>
            Zatvori
          </Button>
        </div>

        <form className="grid gap-4 p-4 md:p-6" onSubmit={onSubmit}>
          {formError ? (
            <div className="rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
              {formError}
            </div>
          ) : null}

          {loading ? (
            <div className="rounded-md border border-hairline bg-surface-2 px-3 py-4 text-sm text-ink-subtle">
              Ucitavanje podataka dokumenta...
            </div>
          ) : null}

          <div className="grid gap-4 lg:grid-cols-2">
            <div className="grid gap-4 rounded-lg border border-hairline bg-surface-2 p-4">
              <div className="text-sm font-semibold uppercase tracking-wide text-ink-muted">Opste</div>

              <TextInput
                label="Naziv *"
                name="naslov"
                required
                disabled={submitting}
                value={values.naslov}
                onChange={(event) => onChange((previous) => ({ ...previous, naslov: event.target.value }))}
              />

              <div className="grid gap-1 rounded-md border border-hairline bg-surface-1 px-3 py-2">
                <span className="text-[13px] font-medium text-ink-muted">Autor *</span>
                <div className="text-sm text-ink">{authorLabel}</div>
              </div>

              <SelectField
                label="Projekat *"
                name="projektId"
                required
                disabled={submitting}
                value={values.projektId}
                onChange={(event) => onChange((previous) => ({ ...previous, projektId: event.target.value }))}
              >
                <option value="">Izaberi projekat</option>
                {projects.map((project) => (
                  <option key={project.id ?? project.name} value={project.id ?? ''}>
                    {project.name}
                  </option>
                ))}
              </SelectField>

              <SelectField
                label="Tip dokumenta *"
                name="tipDokumentaId"
                required
                disabled={submitting}
                value={values.tipDokumentaId}
                onChange={(event) =>
                  onChange((previous) => ({
                    ...previous,
                    tipDokumentaId: event.target.value,
                    metapodaci: [],
                  }))
                }
              >
                <option value="">Izaberi tip dokumenta</option>
                {tipoviDokumenta.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.naziv}
                  </option>
                ))}
              </SelectField>

              <TextArea
                label="Sadrzaj"
                name="sadrzaj"
                disabled={submitting}
                value={values.sadrzaj}
                onChange={(event) => onChange((previous) => ({ ...previous, sadrzaj: event.target.value }))}
              />
            </div>

            <div className="grid gap-4 rounded-lg border border-hairline bg-surface-2 p-4">
              <div className="text-sm font-semibold uppercase tracking-wide text-ink-muted">Tagovi i metapodaci</div>

              <TextInput
                label="Tagovi"
                name="tagoviText"
                disabled={submitting}
                placeholder="AI zdravstveni izvestaj"
                value={values.tagoviText}
                onChange={(event) => onChange((previous) => ({ ...previous, tagoviText: event.target.value }))}
              />

              <div className="rounded-md border border-hairline bg-surface-1 px-3 py-3">
                <div className="text-xs font-medium uppercase tracking-wide text-ink-subtle">Pregled tagova</div>
                <div className="mt-2 flex min-h-10 flex-wrap gap-2">
                  {tagTokens.length ? (
                    tagTokens.map((tag) => (
                      <span
                        key={tag}
                        className="rounded-full bg-surface-2 px-3 py-1 text-sm text-ink-muted"
                      >
                        {tag}
                      </span>
                    ))
                  ) : (
                    <span className="text-sm text-ink-subtle">Upiši tagove razdvojene razmacima.</span>
                  )}
                </div>
              </div>

              <div className="rounded-lg border border-hairline bg-surface-1 p-3">
                <div className="mb-3">
                  <div className="text-sm font-semibold text-ink">Metapodaci</div>
                </div>

                {!values.tipDokumentaId ? null : selectedMetadataOptions.length === 0 ? null : (
                  <div className="space-y-3">
                    {selectedMetadataOptions.map((item) => {
                      const row = values.metapodaci.find((r) => r.tipMetapodatkaId === item.id)
                      return (
                        <div key={item.id} className="grid gap-1">
                          <label className="text-[13px] font-medium text-ink-muted">
                            {item.naziv} <span className="text-ink-tertiary">({item.tipPodatka})</span>
                          </label>
                          <input
                            type="text"
                            disabled={submitting}
                            value={row?.vrednost ?? ''}
                            onChange={(event) => onMetadataFieldChange(item.id, event.target.value)}
                            className="rounded-md border border-hairline bg-surface-1 px-3 py-2 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50 disabled:opacity-50"
                          />
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 border-t border-hairline pt-4">
            <Button type="button" variant="secondary" onClick={onClose} disabled={submitting}>
              Otkazi
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Cuvanje...' : mode === 'create' ? 'Kreiraj dokument' : 'Azuriraj dokument'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default DocumentsPage
