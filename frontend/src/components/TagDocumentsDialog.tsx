import { useEffect, useRef, useState } from 'react'
import { autoTagDocuments, searchDocumentsForTagging } from '../api/autoTag'
import type { Dokument } from '../types/document'
import { Button } from './ui/Button'

interface TagDocumentsDialogProps {
  isOpen: boolean
  onClose: () => void
  onApplied?: () => void
  documents?: Dokument[]
}

type DialogState = 'initial' | 'results' | 'empty'

export function TagDocumentsDialog({ isOpen, onClose, onApplied, documents = [] }: TagDocumentsDialogProps) {
  const [searchQuery, setSearchQuery] = useState('')
  const [searchLoading, setSearchLoading] = useState(false)
  const [applyLoading, setApplyLoading] = useState(false)
  const [dialogState, setDialogState] = useState<DialogState>('initial')
  const [foundDocumentIds, setFoundDocumentIds] = useState<string[]>([])
  const [selectedDocumentIds, setSelectedDocumentIds] = useState<Set<string>>(new Set())
  const [suggestedTagName, setSuggestedTagName] = useState('')
  const [additionalTagsText, setAdditionalTagsText] = useState('')
  const [searchError, setSearchError] = useState<string | null>(null)
  const [applyError, setApplyError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const searchInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (isOpen) {
      setSearchQuery('')
      setSearchLoading(false)
      setApplyLoading(false)
      setDialogState('initial')
      setFoundDocumentIds([])
      setSelectedDocumentIds(new Set())
      setSuggestedTagName('')
      setAdditionalTagsText('')
      setSearchError(null)
      setApplyError(null)
      setSuccessMessage(null)
      setTimeout(() => searchInputRef.current?.focus(), 50)
    }
  }, [isOpen])

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }
    if (isOpen) {
      document.addEventListener('keydown', handleKeyDown)
      return () => document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen, onClose])

  async function handleSearch() {
    const query = searchQuery.trim()
    if (!query) return

    setSearchLoading(true)
    setSearchError(null)
    setApplyError(null)

    try {
      const response = await searchDocumentsForTagging({ prompt: query, similarityThreshold: 0.60 })
      if (response.count === 0) {
        setDialogState('empty')
        setFoundDocumentIds([])
        setSelectedDocumentIds(new Set())
      } else {
        setFoundDocumentIds(response.documentIds)
        setSelectedDocumentIds(new Set(response.documentIds))
        setSuggestedTagName(response.suggestedTagName)
        setDialogState('results')
      }
    } catch (err) {
      setSearchError(err instanceof Error ? err.message : 'Pretraga nije uspela')
    } finally {
      setSearchLoading(false)
    }
  }

  function toggleDocument(id: string) {
    setSelectedDocumentIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  function toggleAll() {
    if (selectedDocumentIds.size === foundDocumentIds.length) {
      setSelectedDocumentIds(new Set())
    } else {
      setSelectedDocumentIds(new Set(foundDocumentIds))
    }
  }

  async function handleApplyTags() {
    const mainTag = suggestedTagName.trim()
    const activeIds = Array.from(selectedDocumentIds)
    if (!mainTag || activeIds.length === 0) return

    const extra = additionalTagsText
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean)

    const allTags = Array.from(
      new Map([mainTag, ...extra].map((t) => [t.toLowerCase(), t])).values(),
    )

    setApplyLoading(true)
    setApplyError(null)

    try {
      const response = await autoTagDocuments({
        tagNames: allTags,
        documentIds: activeIds,
      })
      setSuccessMessage(
        `Uspešno označeno ${response.taggedDocumentCount} dokumenata tagovima: ${response.appliedTags.join(', ')}`,
      )
      setTimeout(() => { onApplied?.(); onClose() }, 1800)
    } catch (err) {
      setApplyError(err instanceof Error ? err.message : 'Primena tagova nije uspela')
    } finally {
      setApplyLoading(false)
    }
  }

  function handleSearchKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Enter') void handleSearch()
  }

  const canApply =
    dialogState === 'results' &&
    selectedDocumentIds.size > 0 &&
    suggestedTagName.trim().length > 0 &&
    !applyLoading

  if (!isOpen) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="relative w-full max-w-lg rounded-xl border border-hairline bg-surface-1 shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-hairline px-6 py-4">
          <h2 className="text-lg font-semibold text-ink">Poluautomatsko tagovanje</h2>
          <button
            type="button"
            onClick={onClose}
            className="flex h-8 w-8 cursor-pointer items-center justify-center rounded-md text-ink-muted transition-colors hover:bg-surface-2 hover:text-ink"
            aria-label="Zatvori"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden>
              <path
                d="M12 4L4 12M4 4l8 8"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
              />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="flex flex-col gap-4 px-6 py-5">
          {/* Search row */}
          <div>
            <label className="mb-1.5 block text-[13px] font-medium text-ink-muted">
              Unesite ključnu reč ili upit za pronalaženje odgovarajućih dokumenata
            </label>
            <div className="flex gap-2">
              <input
                ref={searchInputRef}
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={handleSearchKeyDown}
                placeholder="npr. mašinsko učenje"
                className="min-h-10 flex-1 rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
              />
              <Button
                type="button"
                variant="secondary"
                onClick={() => void handleSearch()}
                disabled={searchLoading || !searchQuery.trim()}
                className="shrink-0"
              >
                {searchLoading ? (
                  <span className="flex items-center gap-1.5">
                    <svg
                      className="h-4 w-4 animate-spin"
                      viewBox="0 0 24 24"
                      fill="none"
                      aria-hidden
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                      />
                    </svg>
                    Pretraga…
                  </span>
                ) : (
                  'Pretraži'
                )}
              </Button>
            </div>
          </div>

          {/* Search error */}
          {searchError && (
            <div className="rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
              {searchError}
            </div>
          )}

          {/* State: empty results */}
          {dialogState === 'empty' && (
            <div className="rounded-md border border-hairline bg-surface-2 px-4 py-3 text-sm text-ink-muted">
              Nisu pronađeni dokumenti koji odgovaraju upitu
            </div>
          )}

          {/* State: results */}
          {dialogState === 'results' && (
            <>
              {/* Info bar with toggle all */}
              <div className="flex items-center justify-between rounded-md bg-surface-2 px-4 py-2.5">
                <span className="text-sm text-ink">
                  Pronađeno{' '}
                  <span className="font-semibold text-primary">{foundDocumentIds.length}</span>{' '}
                  {foundDocumentIds.length === 1 ? 'dokument' : 'dokumenata'}
                </span>
                <button
                  type="button"
                  onClick={toggleAll}
                  className="cursor-pointer text-xs text-primary hover:underline"
                >
                  {selectedDocumentIds.size === foundDocumentIds.length ? 'Odaberi nijedan' : 'Odaberi sve'}
                </button>
              </div>

              {/* Document list */}
              <div className="max-h-48 overflow-y-auto rounded-md border border-hairline bg-surface-2">
                {foundDocumentIds.map((id) => {
                  const doc = documents.find((d) => d.id === id)
                  const isSelected = selectedDocumentIds.has(id)
                  return (
                    <label
                      key={id}
                      className="flex cursor-pointer items-center gap-3 border-b border-hairline px-3 py-2.5 last:border-b-0 hover:bg-surface-1"
                    >
                      <input
                        type="checkbox"
                        checked={isSelected}
                        onChange={() => toggleDocument(id)}
                        className="h-4 w-4 shrink-0 accent-primary"
                      />
                      <span className="min-w-0 flex-1 truncate text-sm text-ink">
                        {doc?.naslov ?? id}
                      </span>
                    </label>
                  )
                })}
              </div>

              <div className="text-xs text-ink-muted">
                {selectedDocumentIds.size} od {foundDocumentIds.length} odabrano za tagovanje
              </div>

              {/* Suggested tag */}
              <div>
                <label className="mb-1.5 block text-[13px] font-medium text-ink-muted">
                  Predloženi naziv taga (možete izmeniti)
                </label>
                <input
                  type="text"
                  value={suggestedTagName}
                  onChange={(e) => setSuggestedTagName(e.target.value)}
                  className="min-h-10 w-full rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                />
              </div>

              {/* Additional tags */}
              <div>
                <label className="mb-1.5 block text-[13px] font-medium text-ink-muted">
                  Dodaj dodatne tagove (opciono)
                </label>
                <input
                  type="text"
                  value={additionalTagsText}
                  onChange={(e) => setAdditionalTagsText(e.target.value)}
                  placeholder="tag1, tag2, tag3"
                  className="min-h-10 w-full rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
                />
              </div>
            </>
          )}

          {/* Apply error */}
          {applyError && (
            <div className="rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
              {applyError}
            </div>
          )}

          {/* Success message */}
          {successMessage && (
            <div className="rounded-md border border-green-500/35 bg-green-500/10 px-3 py-2 text-sm text-green-400">
              {successMessage}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex justify-end gap-2 border-t border-hairline px-6 py-4">
          <Button type="button" variant="secondary" onClick={onClose}>
            Otkaži
          </Button>
          <Button
            type="button"
            variant="primary"
            onClick={() => void handleApplyTags()}
            disabled={!canApply}
          >
            {applyLoading ? 'Primena…' : 'Primeni tagove'}
          </Button>
        </div>
      </div>
    </div>
  )
}
