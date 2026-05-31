import { useEffect, useMemo, useState, type Dispatch, type FormEvent, type SetStateAction } from 'react'
import { createDocument, deleteDocument, fetchDocuments, updateDocument } from '../../api/documents'
import { searchDocuments as searchVectorDocuments } from '../../api/documentSearch'
import { fetchDocumentTags } from '../../api/documentTags'
import { fetchMetapodatakByDocument } from '../../api/metapodatak'
import { fetchProjectsForSelection } from '../../api/projects'
import { fetchTags } from '../../api/tags'
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

interface MetadataFilterRow {
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
  const [selectedDocumentTypeFilterId, setSelectedDocumentTypeFilterId] = useState('')
  const [authorFilter, setAuthorFilter] = useState('')
  const [tagFilter, setTagFilter] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<Dokument[] | null>(null)
  const [searchLoading, setSearchLoading] = useState(false)
  const [metadataFilters, setMetadataFilters] = useState<MetadataFilterRow[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [tags, setTags] = useState<Tag[]>([])
  const [tipoviDokumenta, setTipoviDokumenta] = useState<TipDokumenta[]>([])
  const [tipoviMetapodataka, setTipoviMetapodataka] = useState<TipMetapodatka[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [dialogOpen, setDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<DocumentMode>('create')
  const [dialogLoading, setDialogLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [activeDocument, setActiveDocument] = useState<Dokument | null>(null)
  const [formValues, setFormValues] = useState<DocumentFormValues>(createEmptyValues())
  const [formError, setFormError] = useState<string | null>(null)

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

  const documentsByVectorId = useMemo(
    () => new Map(documents.filter((document) => document.vectorDocumentId).map((document) => [document.vectorDocumentId as string, document])),
    [documents],
  )

  const searchableMetadataOptions = useMemo(() => {
    if (!selectedDocumentTypeFilterId) {
      return tipoviMetapodataka
    }

    return tipoviMetapodataka.filter((item) => item.tipDokumentaId === selectedDocumentTypeFilterId)
  }, [selectedDocumentTypeFilterId, tipoviMetapodataka])

  const searchMetadataOptionIds = useMemo(
    () => new Set(searchableMetadataOptions.map((item) => item.id)),
    [searchableMetadataOptions],
  )

  const metadataRows = useMemo(
    () => metadataFilters.filter((item) => item.tipMetapodatkaId || item.vrednost.trim()),
    [metadataFilters],
  )

  const sourceDocuments = useMemo(() => {
    if (searchQuery.trim()) {
      return searchResults ?? []
    }

    return documents
  }, [documents, searchQuery, searchResults])

  const selectedMetadataOptions = useMemo(
    () => metadataOptionsForDocumentType(tipoviMetapodataka, formValues.tipDokumentaId),
    [tipoviMetapodataka, formValues.tipDokumentaId],
  )

  const selectedDocument = useMemo(
    () => documents.find((document) => document.id === selectedDocumentId) ?? null,
    [documents, selectedDocumentId],
  )

  const filteredDocuments = useMemo(() => {
    const trimmedAuthorFilter = authorFilter.trim()
    const trimmedTagFilter = tagFilter.trim()
    const normalizedQuery = normalizeSearchText(searchQuery)

    return sourceDocuments.filter((document) => {
      const selectedProject = selectedProjectId ? projects.find((project) => project.id === selectedProjectId) : null
      const documentProjectName = document.projectName ?? projectNameById.get(document.projektId ?? '') ?? ''
      const documentMetadata = documentMetadataByDocumentId.get(document.id) ?? []

      if (selectedProjectId) {
        const matchesProject =
          document.projektId === selectedProjectId ||
          (selectedProject?.name ? documentProjectName === selectedProject.name : false)
        if (!matchesProject) return false
      }

      if (selectedDocumentTypeFilterId && document.tipDokumentaId !== selectedDocumentTypeFilterId) {
        return false
      }

      if (trimmedAuthorFilter) {
        const authorCandidate = `${document.authorName ?? ''} ${document.authorId}`
        if (!valueContains(authorCandidate, trimmedAuthorFilter)) return false
      }

      if (trimmedTagFilter) {
        const tagNames = documentTagNamesByDocumentId.get(document.id) ?? []
        if (!tagNames.some((tagName) => valueContains(tagName, trimmedTagFilter))) return false
      }

      if (normalizedQuery && !searchResults) {
        const searchableText = [
          document.naslov,
          document.sadrzaj ?? '',
          document.authorName ?? '',
          document.authorId,
          document.projectName ?? '',
          document.projektId ?? '',
          ...(documentTagNamesByDocumentId.get(document.id) ?? []),
          ...documentMetadata.map((item) => `${tipMetapodatkaNameById.get(item.tipMetapodatkaId) ?? item.tipMetapodatkaId} ${item.vrednost}`),
        ].join(' ')

        if (!valueContains(searchableText, normalizedQuery)) return false
      }

      for (const row of metadataRows) {
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
    authorFilter,
    documentMetadataByDocumentId,
    documentTagNamesByDocumentId,
    metadataRows,
    projects,
    searchQuery,
    selectedDocumentTypeFilterId,
    selectedProjectId,
    sourceDocuments,
    projectNameById,
    tipMetapodatkaById,
    tipMetapodatkaNameById,
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
    const query = searchQuery.trim()
    if (!query) {
      setSearchResults(null)
      setSearchLoading(false)
      return
    }

    let active = true
    const timeoutId = window.setTimeout(() => {
      setSearchLoading(true)

      void (async () => {
        try {
          const response = await searchVectorDocuments({
            query,
            topK: 50,
            docTypeId: selectedDocumentTypeFilterId || undefined,
            projectId: selectedProjectId || undefined,
          })

          if (!active) return

          const rankedDocuments: Dokument[] = response.results.flatMap((hit) => {
            let document = documentsByVectorId.get(String(hit.id)) ?? documentsById.get(String(hit.id))

            if (!document) {
              document = documents.find((d) =>
                String(d.vectorDocumentId) === String(hit.id) || String(d.id) === String(hit.id) ||
                (hit.title && normalizeSearchText(d.naslov) === normalizeSearchText(String(hit.title)))
              )
            }

            if (!document) return []

            return [
              {
                ...document,
                vectorDocumentId: String(hit.id),
                tags: hit.tags ?? document.tags,
                metadata: hit.metadata ?? document.metadata,
                score: hit.score,
                semanticScore: hit.semantic_score,
                lexicalScore: hit.lexical_score,
              },
            ]
          })

          setSearchResults(rankedDocuments)
        } catch (searchError) {
          if (!active) return
          setSearchResults([])
          setError(searchError instanceof Error ? searchError.message : 'Neuspesna pretraga dokumenata')
        } finally {
          if (active) {
            setSearchLoading(false)
          }
        }
      })()
    }, 300)

    return () => {
      active = false
      window.clearTimeout(timeoutId)
    }
  }, [documentsById, searchQuery, selectedDocumentTypeFilterId, selectedProjectId])

  useEffect(() => {
    setMetadataFilters((current) =>
      current.map((row) =>
        row.tipMetapodatkaId && !searchMetadataOptionIds.has(row.tipMetapodatkaId)
          ? { tipMetapodatkaId: '', vrednost: '' }
          : row,
      ),
    )
  }, [searchMetadataOptionIds])

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

  function clearAllFilters() {
    setSearchQuery('')
    setSelectedProjectId(null)
    setSelectedDocumentTypeFilterId('')
    setAuthorFilter('')
    setTagFilter('')
    setMetadataFilters([])
    setSearchResults(null)
    setSearchLoading(false)
  }

  function updateMetadataRow(index: number, patch: Partial<MetadataRow>) {
    setFormValues((previous) => ({
      ...previous,
      metapodaci: previous.metapodaci.map((item, itemIndex) =>
        itemIndex === index ? { ...item, ...patch } : item,
      ),
    }))
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
          <Button icon="add" onClick={openCreateDialog}>
            Novi dokument
          </Button>
        </div>

        {error ? (
          <div className="rounded-lg border border-error/35 bg-error/10 px-4 py-3 text-sm text-[#ffb4b4]">
            {error}
          </div>
        ) : null}

        <section className="grid gap-4 rounded-lg border border-hairline bg-surface-1 p-4 shadow-sm">
          <div className="grid gap-3 lg:grid-cols-[minmax(0,1.5fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)]">
            <TextInput
              label="Vektorska pretraga"
              name="documentSearch"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Traži po naslovu, sadržaju, tagovima i metapodacima"
            />

            <SelectField
              label="Tip dokumenta"
              value={selectedDocumentTypeFilterId}
              onChange={(event) => setSelectedDocumentTypeFilterId(event.target.value)}
            >
              <option value="">Svi tipovi</option>
              {tipoviDokumenta.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.naziv}
                </option>
              ))}
            </SelectField>

            <TextInput
              label="Autor"
              name="authorFilter"
              value={authorFilter}
              onChange={(event) => setAuthorFilter(event.target.value)}
              placeholder="Ime, prezime ili ID autora"
            />

            <TextInput
              label="Tag"
              name="tagFilter"
              value={tagFilter}
              onChange={(event) => setTagFilter(event.target.value)}
              placeholder="deo naziva taga"
            />
          </div>

          <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_320px]">
            <div className="grid gap-3 rounded-lg border border-hairline bg-surface-2 p-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <div className="text-sm font-semibold text-ink">Filteri po metapodacima</div>
                  <div className="text-xs text-ink-subtle">
                    {selectedDocumentTypeFilterId
                      ? 'Prikazani su samo metapodaci za izabrani tip dokumenta.'
                      : 'Prikazani su svi tipovi metapodataka.'}
                  </div>
                </div>
                <Button
                  type="button"
                  variant="secondary"
                  icon="add"
                  onClick={() =>
                    setMetadataFilters((current) => [...current, { tipMetapodatkaId: '', vrednost: '' }])
                  }
                >
                  Dodaj filter
                </Button>
              </div>

              {metadataFilters.length ? (
                <div className="grid gap-3">
                  {metadataFilters.map((row, index) => (
                    <div
                      key={`metadata-filter-${index}`}
                      className="grid gap-3 rounded-md border border-hairline bg-surface-1 p-3 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] md:items-end"
                    >
                      <SelectField
                        label="Metapodatak"
                        value={row.tipMetapodatkaId}
                        onChange={(event) =>
                          setMetadataFilters((current) =>
                            current.map((item, itemIndex) =>
                              itemIndex === index
                                ? { ...item, tipMetapodatkaId: event.target.value, vrednost: '' }
                                : item,
                            ),
                          )
                        }
                      >
                        <option value="">Izaberi metapodatak</option>
                        {searchableMetadataOptions.map((item) => (
                          <option key={item.id} value={item.id}>
                            {item.naziv} ({item.tipPodatka})
                          </option>
                        ))}
                      </SelectField>

                      <TextInput
                        label="Vrednost"
                        value={row.vrednost}
                        onChange={(event) =>
                          setMetadataFilters((current) =>
                            current.map((item, itemIndex) =>
                              itemIndex === index ? { ...item, vrednost: event.target.value } : item,
                            ),
                          )
                        }
                        placeholder="Unesi vrednost"
                        disabled={!row.tipMetapodatkaId}
                      />

                      <Button
                        type="button"
                        variant="delete"
                        onClick={() =>
                          setMetadataFilters((current) => current.filter((_, itemIndex) => itemIndex !== index))
                        }
                      >
                        Ukloni
                      </Button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="rounded-md border border-dashed border-hairline px-3 py-4 text-sm text-ink-subtle">
                  Dodaj jedan ili više filtera po metapodacima.
                </div>
              )}
            </div>

            <div className="grid gap-3 self-start rounded-lg border border-hairline bg-surface-2 p-4">
              <div className="text-sm font-semibold text-ink">Pregled</div>
              <div className="text-sm text-ink-subtle">
                {searchLoading
                  ? 'Vector pretraga je u toku...'
                  : `${filteredDocuments.length} dokument(a) u rezultatima`}
              </div>
              <Button type="button" variant="secondary" onClick={clearAllFilters}>
                Očisti filtere
              </Button>
            </div>
          </div>
        </section>

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
                  {searchQuery.trim() || authorFilter.trim() || tagFilter.trim() || selectedDocumentTypeFilterId || metadataRows.length || selectedProjectId
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
                    <div className="mt-2 flex flex-wrap gap-2">
                      {(documentTagNamesByDocumentId.get(selectedDocument.id) ?? []).length ? (
                        (documentTagNamesByDocumentId.get(selectedDocument.id) ?? []).map((tagName) => (
                          <span key={`panel-${selectedDocument.id}-${tagName}`} className="rounded-full bg-surface-1 px-2 py-1 text-xs text-ink-subtle">
                            {tagName}
                          </span>
                        ))
                      ) : (
                        <span className="text-sm text-ink-subtle">Nema tagova</span>
                      )}
                    </div>
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
          tagNameById={tagNameById}
          tipDokumentaNameById={tipDokumentaNameById}
          authorLabel={currentAuthorLabel}
          onClose={closeDialog}
          onSubmit={handleSubmit}
          onChange={setFormValues}
          onAddMetadataRow={() =>
            setFormValues((previous) => ({
              ...previous,
              metapodaci: [...previous.metapodaci, { tipMetapodatkaId: '', vrednost: '' }],
            }))
          }
          onRemoveMetadataRow={(index) =>
            setFormValues((previous) => ({
              ...previous,
              metapodaci: previous.metapodaci.filter((_, itemIndex) => itemIndex !== index),
            }))
          }
          onUpdateMetadataRow={updateMetadataRow}
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
  tagNameById: Map<string, string>
  tipDokumentaNameById: Map<string, string>
  authorLabel: string
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: Dispatch<SetStateAction<DocumentFormValues>>
  onAddMetadataRow: () => void
  onRemoveMetadataRow: (index: number) => void
  onUpdateMetadataRow: (index: number, patch: Partial<MetadataRow>) => void
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
  tagNameById,
  tipDokumentaNameById,
  authorLabel,
  onClose,
  onSubmit,
  onChange,
  onAddMetadataRow,
  onRemoveMetadataRow,
  onUpdateMetadataRow,
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
                <span className="text-[13px] font-medium text-ink-muted">Author *</span>
                <div className="text-sm text-ink">
                  {authorLabel}
                </div>
                <div className="text-xs text-ink-subtle">
                  Autor se automatski uzima iz prijavljene sesije.
                </div>
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
                    <span className="text-sm text-ink-subtle">Upisi tagove razdvojene razmacima.</span>
                  )}
                </div>
                <div className="mt-2 text-xs text-ink-subtle">
                  Ucitani tagovi sa backend-a: {tagsAsText(tagNameById)}
                </div>
              </div>

              <div className="rounded-lg border border-hairline bg-surface-1 p-3">
                <div className="mb-3 flex items-center justify-between gap-3">
                  <div>
                    <div className="text-sm font-semibold text-ink">Opcioni metapodaci</div>
                    <div className="text-xs text-ink-subtle">
                      Prvo izaberi tip dokumenta, zatim dodaj redove i odaberi tipove metapodataka koji mu pripadaju.
                    </div>
                  </div>
                  <Button type="button" variant="secondary" icon="add" onClick={onAddMetadataRow} disabled={submitting || !values.tipDokumentaId}>
                    Dodaj
                  </Button>
                </div>

                {!values.tipDokumentaId ? (
                  <div className="rounded-md border border-dashed border-hairline px-3 py-4 text-sm text-ink-subtle">
                    Izaberi tip dokumenta da ucitas njegove tipove metapodataka.
                  </div>
                ) : selectedMetadataOptions.length === 0 ? (
                  <div className="rounded-md border border-dashed border-hairline px-3 py-4 text-sm text-ink-subtle">
                    Za ovaj tip dokumenta nisu podeseni tipovi metapodataka.
                  </div>
                ) : values.metapodaci.length === 0 ? (
                  <div className="rounded-md border border-dashed border-hairline px-3 py-4 text-sm text-ink-subtle">
                    Jos nema redova metapodataka.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {values.metapodaci.map((row, index) => (
                      <div key={`metadata-${index}`} className="rounded-md border border-hairline bg-surface-2 p-3">
                        <div className="grid gap-3 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] md:items-end">
                          <SelectField
                            label="Tip metapodatka"
                            name={`metadataType-${index}`}
                            disabled={submitting}
                            value={row.tipMetapodatkaId}
                            onChange={(event) =>
                              onUpdateMetadataRow(index, { tipMetapodatkaId: event.target.value })
                            }
                          >
                            <option value="">Izaberi tip metapodatka</option>
                            {selectedMetadataOptions.map((item) => (
                              <option key={item.id} value={item.id}>
                                {item.naziv} ({item.tipPodatka})
                              </option>
                            ))}
                          </SelectField>

                          <TextInput
                            label="Vrednost"
                            name={`metadataValue-${index}`}
                            disabled={submitting}
                            value={row.vrednost}
                            onChange={(event) => onUpdateMetadataRow(index, { vrednost: event.target.value })}
                          />

                          <Button
                            type="button"
                            variant="delete"
                            disabled={submitting}
                            onClick={() => onRemoveMetadataRow(index)}
                          >
                            Ukloni
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="rounded-md border border-hairline bg-surface-1 px-3 py-3 text-sm text-ink-muted">
                <div className="font-medium text-ink">Pregled</div>
                <div className="mt-1 text-xs text-ink-subtle">
                  Projekat: {projects.find((project) => project.id === values.projektId)?.name ?? 'N/A'}
                </div>
                <div className="mt-1 text-xs text-ink-subtle">
                  Tip: {tipDokumentaNameById.get(values.tipDokumentaId) ?? 'N/A'}
                </div>
                <div className="mt-1 text-xs text-ink-subtle">
                  Dostupnih tipova metapodataka: {selectedMetadataOptions.length}
                </div>
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

function tagsAsText(tagNameById: Map<string, string>) {
  const values = Array.from(tagNameById.values())
  if (!values.length) return 'Tagovi jos nisu ucitani.'
  return values.join(' • ')
}

export default DocumentsPage
