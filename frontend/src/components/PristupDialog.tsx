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
} from '../api/pristup'
import type { NivoPrava, PravaPristupa } from '../types/pravaPristupa'

interface PristupDialogProps {
  isOpen: boolean
  onClose: () => void
  resourceType: 'DOKUMENT' | 'PROJEKAT'
  resourceId: string
  currentUserId: string
}

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

export function PristupDialog({
  isOpen,
  onClose,
  resourceType,
  resourceId,
  currentUserId,
}: PristupDialogProps) {
  const [pristup, setPristup] = useState<PravaPristupa[]>([])
  const [korisnici, setKorisnici] = useState<KorisnikInfo[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showAddForm, setShowAddForm] = useState(false)
  const [newKorisnikId, setNewKorisnikId] = useState('')
  const [newNivo, setNewNivo] = useState<NivoPrava>('CITANJE')
  const [addLoading, setAddLoading] = useState(false)
  const [addError, setAddError] = useState<string | null>(null)
  const [editingNivoId, setEditingNivoId] = useState<string | null>(null)
  const [editNivoValue, setEditNivoValue] = useState<NivoPrava>('CITANJE')
  const [editNivoLoading, setEditNivoLoading] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const [data, users] = await Promise.all([
        resourceType === 'DOKUMENT'
          ? getDokumentAccess(resourceId)
          : getProjekatAccess(resourceId),
        fetchAllUsers(),
      ])
      setPristup(Array.isArray(data) ? data : [])
      setKorisnici(users)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Greška pri učitavanju')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (isOpen) {
      void load()
      setShowAddForm(false)
      setNewKorisnikId('')
      setNewNivo('CITANJE')
      setAddError(null)
      setEditingNivoId(null)
    }
  }, [isOpen, resourceId, resourceType])

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    if (isOpen) {
      document.addEventListener('keydown', handleKeyDown)
      return () => document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen, onClose])

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
      setShowAddForm(false)
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
    setEditNivoLoading(true)
    try {
      if (resourceType === 'DOKUMENT') {
        await grantDokumentAccess(resourceId, korisnikId, noviNivo, currentUserId)
      } else {
        await grantProjekatAccess(resourceId, korisnikId, noviNivo, currentUserId)
      }
      setEditingNivoId(null)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Greška pri izmeni nivoa pristupa')
    } finally {
      setEditNivoLoading(false)
    }
  }

  function resolveUserLabel(korisnikId: string) {
    const k = korisnici.find((u) => u.uuid === korisnikId || String(u.id) === korisnikId)
    return k ? userLabel(k) : korisnikId
  }

  const availableKorisnici = korisnici.filter(
    (k) => !pristup.some((p) => p.korisnikId === k.uuid || p.korisnikId === String(k.id)),
  )

  if (!isOpen) return null

  const title = resourceType === 'DOKUMENT' ? 'Pristup dokumentu' : 'Pristup projektu'

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="relative w-full max-w-lg rounded-xl border border-hairline bg-surface-1 shadow-2xl">
        <div className="flex items-center justify-between border-b border-hairline px-6 py-4">
          <h2 className="text-lg font-semibold text-ink">{title}</h2>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => { setShowAddForm((v) => !v); setAddError(null) }}
              className="rounded-md border border-hairline bg-surface-2 px-3 py-1.5 text-sm text-ink-muted hover:bg-surface-1 hover:text-ink cursor-pointer"
            >
              Dodaj
            </button>
            <button
              type="button"
              onClick={onClose}
              className="flex h-8 w-8 cursor-pointer items-center justify-center rounded-md text-ink-muted transition-colors hover:bg-surface-2 hover:text-ink"
              aria-label="Zatvori"
            >
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden>
                <path d="M12 4L4 12M4 4l8 8" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" />
              </svg>
            </button>
          </div>
        </div>

        <div className="flex flex-col gap-3 px-6 py-5">
          {showAddForm && (
            <div className="flex flex-col gap-2 rounded-md border border-hairline bg-surface-2 p-3">
              <select
                value={newKorisnikId}
                onChange={(e) => setNewKorisnikId(e.target.value)}
                className="min-h-9 rounded-md border border-hairline bg-surface-1 px-3 py-2 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
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
              {addError && (
                <div className="text-xs text-[#ffb4b4]">{addError}</div>
              )}
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => void handleAdd()}
                  disabled={addLoading || !newKorisnikId}
                  className="rounded-md border border-hairline bg-primary/20 px-3 py-1.5 text-sm text-primary hover:bg-primary/30 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
                >
                  {addLoading ? 'Dodavanje…' : 'Potvrdi'}
                </button>
                <button
                  type="button"
                  onClick={() => { setShowAddForm(false); setAddError(null) }}
                  className="rounded-md border border-hairline px-3 py-1.5 text-sm text-ink-muted hover:bg-surface-2 cursor-pointer"
                >
                  Otkaži
                </button>
              </div>
            </div>
          )}

          {error && (
            <div className="rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
              {error}
            </div>
          )}

          {loading ? (
            <div className="py-6 text-center text-sm text-ink-muted">Učitavanje...</div>
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
                      <div className="mt-1 text-xs text-ink-muted">
                        Dodao: {resolveUserLabel(item.dodeljivaoId)} &nbsp;·&nbsp; Datum: {formatDate(item.datumDodele)}
                      </div>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <NivoBadge nivo={item.nivo} />
                      <button
                        type="button"
                        onClick={() => {
                          if (editingNivoId === item.id) {
                            setEditingNivoId(null)
                          } else {
                            setEditingNivoId(item.id)
                            setEditNivoValue(item.nivo)
                          }
                        }}
                        className="flex h-7 w-7 shrink-0 cursor-pointer items-center justify-center rounded-md text-ink-muted hover:bg-surface-2 hover:text-ink"
                        aria-label="Izmeni nivo pristupa"
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
                        className="flex h-7 w-7 shrink-0 cursor-pointer items-center justify-center rounded-md text-ink-muted hover:bg-error/15 hover:text-error"
                        aria-label="Ukloni pristup"
                        title="Ukloni pristup"
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden>
                          <path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                      </button>
                    </div>
                  </div>
                  {editingNivoId === item.id && (
                    <div className="flex items-center gap-2 rounded-md border border-hairline bg-surface-2 px-3 py-2">
                      <select
                        value={editNivoValue}
                        onChange={(e) => setEditNivoValue(e.target.value as NivoPrava)}
                        disabled={editNivoLoading}
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
                        onClick={() => void handleChangeNivo(item.korisnikId, editNivoValue)}
                        disabled={editNivoLoading || editNivoValue === item.nivo}
                        className="rounded-md border border-hairline bg-primary/20 px-3 py-1 text-xs text-primary hover:bg-primary/30 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
                      >
                        {editNivoLoading ? 'Čuvanje…' : 'Potvrdi'}
                      </button>
                      <button
                        type="button"
                        onClick={() => setEditingNivoId(null)}
                        disabled={editNivoLoading}
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
      </div>
    </div>
  )
}
