import { useEffect, useMemo, useState } from 'react'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { SelectField } from '../../components/ui/SelectField'
import { TextInput } from '../../components/ui/TextInput'
import { createTipDokumenta, fetchTipDokumenta } from '../../api/tipDokumenta'
import { createTipMetapodatka, fetchTipMetapodataka } from '../../api/tipMetapodatka'
import type { TipDokumenta } from '../../types/tipDokumenta'
import type { TipMetapodatka, TipPodatka } from '../../types/tipMetapodatka'

const TIPOVI_PODATAKA: TipPodatka[] = ['TEKST', 'BROJ', 'DATUM', 'BOOLEAN', 'LISTA']

function byName<T extends { naziv: string }>(left: T, right: T) {
  return left.naziv.localeCompare(right.naziv)
}

export function DocumentTypesPage() {
  const [tipoviDokumenata, setTipoviDokumenata] = useState<TipDokumenta[]>([])
  const [tipoviMetapodataka, setTipoviMetapodataka] = useState<TipMetapodatka[]>([])
  const [loading, setLoading] = useState(true)
  const [savingDocumentType, setSavingDocumentType] = useState(false)
  const [savingMetadataType, setSavingMetadataType] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [selectedDocumentTypeId, setSelectedDocumentTypeId] = useState('')
  const [documentTypeName, setDocumentTypeName] = useState('')
  const [metadataName, setMetadataName] = useState('')
  const [metadataTipPodatka, setMetadataTipPodatka] = useState<TipPodatka>('TEKST')
  const [metadataRequired, setMetadataRequired] = useState(true)

  async function loadData() {
    setLoading(true)
    setError('')

    try {
      const [documentTypes, metadataTypes] = await Promise.all([
        fetchTipDokumenta(),
        fetchTipMetapodataka(),
      ])

      const sortedDocumentTypes = [...documentTypes].sort(byName)
      setTipoviDokumenata(sortedDocumentTypes)
      setTipoviMetapodataka([...metadataTypes].sort(byName))
      setSelectedDocumentTypeId((current) => {
        if (current && sortedDocumentTypes.some((item) => item.id === current)) {
          return current
        }

        return sortedDocumentTypes[0]?.id ?? ''
      })
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Failed to load document types.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadData()
  }, [])

  const metadataForSelectedDocumentType = useMemo(
    () => tipoviMetapodataka.filter((item) => item.tipDokumentaId === selectedDocumentTypeId),
    [selectedDocumentTypeId, tipoviMetapodataka],
  )

  async function handleCreateDocumentType(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setMessage('')

    const naziv = documentTypeName.trim()
    if (!naziv) {
      setError('Type name is required.')
      return
    }

    setSavingDocumentType(true)
    try {
      const created = await createTipDokumenta(naziv)
      setTipoviDokumenata((current) => [...current, created].sort(byName))
      setSelectedDocumentTypeId(created.id)
      setDocumentTypeName('')
      setMessage('Document type created.')
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'Could not create document type.')
    } finally {
      setSavingDocumentType(false)
    }
  }

  async function handleCreateMetadataType(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setMessage('')

    const naziv = metadataName.trim()
    if (!selectedDocumentTypeId) {
      setError('Create a document type first.')
      return
    }

    if (!naziv) {
      setError('Metadata name is required.')
      return
    }

    setSavingMetadataType(true)
    try {
      const created = await createTipMetapodatka({
        naziv,
        tipPodatka: metadataTipPodatka,
        jeObavezan: metadataRequired,
        tipDokumentaId: selectedDocumentTypeId,
      })
      setTipoviMetapodataka((current) => [...current, created].sort(byName))
      setMetadataName('')
      setMetadataTipPodatka('TEKST')
      setMetadataRequired(true)
      setMessage('Metadata type created.')
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'Could not create metadata type.')
    } finally {
      setSavingMetadataType(false)
    }
  }

  const selectedDocumentType = tipoviDokumenata.find((item) => item.id === selectedDocumentTypeId)

  return (
    <AppShell>
      <div className="mx-auto grid max-w-6xl gap-6">
        <div className="grid gap-2">
          <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink">
            Tipovi dokumenata
          </h1>
          <p className="m-0 max-w-2xl text-sm text-ink-subtle">
            Ovde admin pravi tip dokumenta i obavezna metadata polja koja članovi kasnije koriste
            pri unosu dokumenta.
          </p>
        </div>

        {error && (
          <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
            {error}
          </p>
        )}

        {message && (
          <p className="m-0 rounded-md border border-emerald-500/30 bg-emerald-500/10 px-3 py-3 text-sm text-emerald-200">
            {message}
          </p>
        )}

        <div className="grid gap-4 lg:grid-cols-[1fr_1.1fr]">
          <section className="grid gap-4 rounded-xl border border-hairline bg-surface-1 p-5 shadow-[0_12px_28px_rgba(0,0,0,0.12)]">
            <div className="grid gap-1">
              <h2 className="m-0 text-lg font-semibold text-ink">Novi tip dokumenta</h2>
              <p className="m-0 text-sm text-ink-subtle">Samo naziv je potreban.</p>
            </div>

            <form className="grid gap-4" onSubmit={handleCreateDocumentType}>
              <TextInput
                label="Naziv tipa dokumenta"
                value={documentTypeName}
                onChange={(event) => setDocumentTypeName(event.target.value)}
                placeholder="npr. Ugovor"
              />

              <Button type="submit" disabled={savingDocumentType || loading}>
                {savingDocumentType ? 'Čuvam...' : 'Napravi tip dokumenta'}
              </Button>
            </form>
          </section>

          <section className="grid gap-4 rounded-xl border border-hairline bg-surface-1 p-5 shadow-[0_12px_28px_rgba(0,0,0,0.12)]">
            <div className="grid gap-1">
              <h2 className="m-0 text-lg font-semibold text-ink">Novo obavezno polje</h2>
              <p className="m-0 text-sm text-ink-subtle">
                Prvo izaberi tip dokumenta, pa dodaj metadata koja mu pripadaju.
              </p>
            </div>

            <form className="grid gap-4" onSubmit={handleCreateMetadataType}>
              <SelectField
                label="Tip dokumenta"
                value={selectedDocumentTypeId}
                onChange={(event) => setSelectedDocumentTypeId(event.target.value)}
                disabled={tipoviDokumenata.length === 0}
              >
                <option value="">Izaberi tip dokumenta</option>
                {tipoviDokumenata.map((tip) => (
                  <option key={tip.id} value={tip.id}>
                    {tip.naziv}
                  </option>
                ))}
              </SelectField>

              <TextInput
                label="Naziv metapodatka"
                value={metadataName}
                onChange={(event) => setMetadataName(event.target.value)}
                placeholder="npr. Broj ugovora"
                disabled={!selectedDocumentTypeId}
              />

              <SelectField
                label="Tip podatka"
                value={metadataTipPodatka}
                onChange={(event) => setMetadataTipPodatka(event.target.value as TipPodatka)}
                disabled={!selectedDocumentTypeId}
              >
                {TIPOVI_PODATAKA.map((tip) => (
                  <option key={tip} value={tip}>
                    {tip}
                  </option>
                ))}
              </SelectField>

              <label className="flex items-center gap-3 rounded-md border border-hairline bg-surface-2 px-3 py-3 text-sm text-ink">
                <input
                  type="checkbox"
                  checked={metadataRequired}
                  onChange={(event) => setMetadataRequired(event.target.checked)}
                />
                Obavezno polje
              </label>

              <Button type="submit" disabled={savingMetadataType || !selectedDocumentTypeId || loading}>
                {savingMetadataType ? 'Čuvam...' : 'Napravi metapodatak'}
              </Button>
            </form>
          </section>
        </div>

        <section className="grid gap-4 rounded-xl border border-hairline bg-surface-1 p-5 shadow-[0_12px_28px_rgba(0,0,0,0.12)]">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div className="grid gap-1">
              <h2 className="m-0 text-lg font-semibold text-ink">Postojeći tipovi</h2>
              <p className="m-0 text-sm text-ink-subtle">
                {selectedDocumentType
                  ? `Izabran tip: ${selectedDocumentType.naziv}`
                  : 'Nema izabranog tipa dokumenta.'}
              </p>
            </div>
            <Button variant="secondary" type="button" onClick={() => void loadData()} disabled={loading}>
              Osveži
            </Button>
          </div>

          <div className="grid gap-3 lg:grid-cols-[1fr_1.1fr]">
            <div className="grid gap-2">
              <h3 className="m-0 text-sm font-semibold uppercase tracking-wide text-ink-subtle">
                Tipovi dokumenata
              </h3>
              <div className="grid gap-2">
                {tipoviDokumenata.length === 0 ? (
                  <p className="m-0 text-sm text-ink-subtle">Još nema unetih tipova.</p>
                ) : (
                  tipoviDokumenata.map((tip) => (
                    <button
                      key={tip.id}
                      type="button"
                      className={`rounded-md border px-3 py-3 text-left transition-colors ${
                        tip.id === selectedDocumentTypeId
                          ? 'border-primary bg-primary/10 text-ink'
                          : 'border-hairline bg-surface-2 text-ink hover:border-hairline-strong'
                      }`}
                      onClick={() => setSelectedDocumentTypeId(tip.id)}
                    >
                      <div className="font-medium">{tip.naziv}</div>
                      <div className="text-xs text-ink-subtle">ID: {tip.id}</div>
                    </button>
                  ))
                )}
              </div>
            </div>

            <div className="grid gap-2">
              <h3 className="m-0 text-sm font-semibold uppercase tracking-wide text-ink-subtle">
                Metapodaci za izabrani tip
              </h3>
              <div className="grid gap-2">
                {selectedDocumentTypeId ? (
                  metadataForSelectedDocumentType.length === 0 ? (
                    <p className="m-0 text-sm text-ink-subtle">
                      Nema dodatih metapodataka za ovaj tip dokumenta.
                    </p>
                  ) : (
                    metadataForSelectedDocumentType.map((metadata) => (
                      <div
                        key={metadata.id}
                        className="grid gap-1 rounded-md border border-hairline bg-surface-2 px-3 py-3"
                      >
                        <div className="flex flex-wrap items-center justify-between gap-2">
                          <strong className="text-sm text-ink">{metadata.naziv}</strong>
                          <span className="text-xs text-ink-subtle">{metadata.tipPodatka}</span>
                        </div>
                        <div className="text-xs text-ink-subtle">
                          {metadata.jeObavezan ? 'Obavezno polje' : 'Opcionalno polje'}
                        </div>
                      </div>
                    ))
                  )
                ) : (
                  <p className="m-0 text-sm text-ink-subtle">
                    Izaberi tip dokumenta da vidiš njegova polja.
                  </p>
                )}
              </div>
            </div>
          </div>
        </section>
      </div>
    </AppShell>
  )
}