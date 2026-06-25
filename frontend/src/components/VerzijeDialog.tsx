import { useEffect, useState } from 'react'
import { getVerzije, getVerzija, restoreVerzija } from '../api/verzije'
import type { DokumentVerzija, DokumentVerzijaFull } from '../types/verzija'

interface Props {
  isOpen: boolean
  onClose: () => void
  dokumentId: string
  currentUserId: string
  canEdit: boolean
  onRestored?: () => void
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('sr-RS')
}

export function VerzijeDialog({ isOpen, onClose, dokumentId, currentUserId, canEdit, onRestored }: Props) {
  const [verzije, setVerzije] = useState<DokumentVerzija[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [expandedId, setExpandedId] = useState<string | null>(null)
  const [fullContent, setFullContent] = useState<Record<string, string>>({})
  const [loadingFullId, setLoadingFullId] = useState<string | null>(null)
  const [confirmRestoreId, setConfirmRestoreId] = useState<string | null>(null)
  const [restoring, setRestoring] = useState(false)
  const [restoreError, setRestoreError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  useEffect(() => {
    if (!isOpen) return
    setVerzije([])
    setError(null)
    setExpandedId(null)
    setFullContent({})
    setConfirmRestoreId(null)
    setRestoreError(null)
    setSuccessMsg(null)
    setLoading(true)
    getVerzije(dokumentId, currentUserId)
      .then(setVerzije)
      .catch((err) => setError(err instanceof Error ? err.message : 'Greška pri učitavanju'))
      .finally(() => setLoading(false))
  }, [isOpen, dokumentId, currentUserId])

  async function handleLoadFull(verzijaId: string) {
    if (fullContent[verzijaId] !== undefined) return
    setLoadingFullId(verzijaId)
    try {
      const full: DokumentVerzijaFull = await getVerzija(dokumentId, verzijaId, currentUserId)
      setFullContent((prev) => ({ ...prev, [verzijaId]: full.sadrzaj ?? '' }))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Greška pri učitavanju sadržaja')
    } finally {
      setLoadingFullId(null)
    }
  }

  async function handleRestore(verzijaId: string) {
    setRestoring(true)
    setRestoreError(null)
    try {
      await restoreVerzija(dokumentId, verzijaId, currentUserId)
      setSuccessMsg('Dokument je vraćen na izabranu verziju.')
      setConfirmRestoreId(null)
      onRestored?.()
      setTimeout(onClose, 1500)
    } catch (err) {
      setRestoreError(err instanceof Error ? err.message : 'Greška pri obnavljanju verzije')
    } finally {
      setRestoring(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/65 p-4">
      <div className="mx-auto my-10 w-full max-w-2xl rounded-lg border border-hairline bg-surface-1 shadow-2xl">
        <div className="flex items-center justify-between border-b border-hairline px-4 py-3">
          <h2 className="m-0 text-xl font-semibold text-ink">Istorija verzija</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md border border-hairline px-2 py-1 text-sm text-ink-muted hover:bg-surface-2"
          >
            Zatvori
          </button>
        </div>

        <div className="p-4">
          {successMsg ? (
            <div className="mb-3 rounded-md border border-green-500/35 bg-green-500/10 px-3 py-2 text-sm text-green-400">
              {successMsg}
            </div>
          ) : null}

          {restoreError ? (
            <div className="mb-3 rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
              {restoreError}
            </div>
          ) : null}

          {error ? (
            <div className="rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
              {error}
            </div>
          ) : loading ? (
            <div className="py-6 text-center text-sm text-ink-subtle">Učitavanje verzija...</div>
          ) : verzije.length === 0 ? (
            <div className="py-6 text-center text-sm text-ink-subtle">Nema sačuvanih verzija.</div>
          ) : (
            <div className="space-y-2">
              {verzije.map((v) => {
                const isExpanded = expandedId === v.id
                const isConfirming = confirmRestoreId === v.id

                return (
                  <div key={v.id} className="rounded-lg border border-hairline bg-surface-2">
                    <div className="flex items-center gap-3 px-3 py-3">
                      <div className="min-w-0 flex-1">
                        <div className="text-sm font-medium text-ink">
                          Verzija {v.verzijaBroj}
                          <span className="ml-2 font-normal text-ink-muted">— {v.naslov}</span>
                        </div>
                        <div className="mt-0.5 text-xs text-ink-subtle">{formatDate(v.datumKreiranja)}</div>
                      </div>

                      <div className="flex shrink-0 gap-2">
                        <button
                          type="button"
                          onClick={() => setExpandedId(isExpanded ? null : v.id)}
                          className="rounded border border-hairline bg-surface-1 px-2.5 py-1 text-xs text-ink-muted hover:bg-surface-2 hover:text-ink"
                        >
                          {isExpanded ? 'Zatvori' : 'Pregledaj'}
                        </button>

                        {canEdit && (
                          <button
                            type="button"
                            onClick={() => {
                              setConfirmRestoreId(isConfirming ? null : v.id)
                              setRestoreError(null)
                            }}
                            className="rounded border border-hairline bg-surface-1 px-2.5 py-1 text-xs text-ink-muted hover:bg-surface-2 hover:text-ink"
                          >
                            Restore
                          </button>
                        )}
                      </div>
                    </div>

                    {isConfirming && (
                      <div className="border-t border-hairline px-3 py-3">
                        <p className="mb-2 text-sm text-ink">
                          Da li ste sigurni? Trenutna verzija će biti sačuvana pre obnavljanja.
                        </p>
                        <div className="flex gap-2">
                          <button
                            type="button"
                            disabled={restoring}
                            onClick={() => void handleRestore(v.id)}
                            className="rounded border border-primary/50 bg-primary/10 px-3 py-1 text-xs text-primary hover:bg-primary/20 disabled:opacity-50"
                          >
                            {restoring ? 'Obnavljanje...' : 'Potvrdi'}
                          </button>
                          <button
                            type="button"
                            disabled={restoring}
                            onClick={() => setConfirmRestoreId(null)}
                            className="rounded border border-hairline bg-surface-1 px-3 py-1 text-xs text-ink-muted hover:bg-surface-2 disabled:opacity-50"
                          >
                            Odustani
                          </button>
                        </div>
                      </div>
                    )}

                    {isExpanded && (
                      <div className="border-t border-hairline px-3 py-3">
                        <div className="rounded-md border border-hairline bg-surface-1 p-3 text-xs text-ink-muted whitespace-pre-wrap break-words">
                          {fullContent[v.id] !== undefined
                            ? (fullContent[v.id] || 'Nema sadržaja.')
                            : (
                              <>
                                <span>{v.sadrzajPreview || 'Nema sadržaja.'}</span>
                                {v.sadrzajPreview && v.sadrzajPreview.length >= 200 && (
                                  <button
                                    type="button"
                                    disabled={loadingFullId === v.id}
                                    onClick={() => void handleLoadFull(v.id)}
                                    className="mt-2 block text-xs text-primary hover:underline disabled:opacity-50"
                                  >
                                    {loadingFullId === v.id ? 'Učitavanje...' : 'Učitaj ceo sadržaj'}
                                  </button>
                                )}
                              </>
                            )}
                        </div>
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
