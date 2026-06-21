import { useEffect, useState } from 'react'
import type { Project } from '../types/project'
import type { TipDokumenta } from '../types/tipDokumenta'
import type { TipMetapodatka } from '../types/tipMetapodatka'
import { Button } from './ui/Button'

export interface MetadataFilterRow {
  tipMetapodatkaId: string
  vrednost: string
}

export interface AdvancedFilters {
  title: string
  author: string
  tipDokumentaId: string
  tag: string
  dateFrom: string
  dateTo: string
  projektId: string
  metadataFilters: MetadataFilterRow[]
}

interface AdvancedSearchDialogProps {
  isOpen: boolean
  filters: AdvancedFilters
  projects: Project[]
  tipoviDokumenta: TipDokumenta[]
  tipoviMetapodataka: TipMetapodatka[]
  onApply: (filters: AdvancedFilters) => void
  onClose: () => void
}

export const EMPTY_ADVANCED_FILTERS: AdvancedFilters = {
  title: '',
  author: '',
  tipDokumentaId: '',
  tag: '',
  dateFrom: '',
  dateTo: '',
  projektId: '',
  metadataFilters: [],
}

export function AdvancedSearchDialog({
  isOpen, filters, projects, tipoviDokumenta, tipoviMetapodataka, onApply, onClose,
}: AdvancedSearchDialogProps) {
  const [local, setLocal] = useState<AdvancedFilters>(filters)

  useEffect(() => {
    if (isOpen) setLocal(filters)
  }, [isOpen, filters])

  useEffect(() => {
    function onKey(e: KeyboardEvent) { if (e.key === 'Escape') onClose() }
    if (isOpen) document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [isOpen, onClose])

  function set<K extends keyof AdvancedFilters>(field: K, value: AdvancedFilters[K]) {
    setLocal((prev) => ({ ...prev, [field]: value }))
  }

  function setTipDokumenta(id: string) {
    setLocal((prev) => ({ ...prev, tipDokumentaId: id, metadataFilters: [] }))
  }

  function setMetadataField(tipMetapodatkaId: string, vrednost: string) {
    setLocal((prev) => {
      const exists = prev.metadataFilters.some((r) => r.tipMetapodatkaId === tipMetapodatkaId)
      return {
        ...prev,
        metadataFilters: exists
          ? prev.metadataFilters.map((r) => r.tipMetapodatkaId === tipMetapodatkaId ? { ...r, vrednost } : r)
          : [...prev.metadataFilters, { tipMetapodatkaId, vrednost }],
      }
    })
  }

  const availableMetadata = local.tipDokumentaId
    ? tipoviMetapodataka.filter((t) => !t.tipDokumentaId || t.tipDokumentaId === local.tipDokumentaId)
    : []

  if (!isOpen) return null

  const inputCls =
    'w-full rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink placeholder:text-ink-tertiary focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50'
  const selectCls =
    'w-full rounded-md border border-hairline bg-surface-2 px-3 py-2 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50'
  const labelCls = 'block text-[13px] font-medium text-ink-muted mb-1'

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="w-full max-w-xl rounded-xl border border-hairline bg-surface-1 shadow-2xl max-h-[90vh] flex flex-col">
        {/* Zaglavlje */}
        <div className="flex items-center justify-between border-b border-hairline px-6 py-4 shrink-0">
          <h2 className="text-lg font-semibold text-ink">Napredna pretraga</h2>
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

        {/* Polja */}
        <div className="overflow-y-auto flex-1 px-6 py-5 space-y-4">
          <div className="grid grid-cols-2 gap-x-4 gap-y-4">
            <div>
              <label className={labelCls}>Naziv</label>
              <input className={inputCls} value={local.title} onChange={(e) => set('title', e.target.value)} />
            </div>
            <div>
              <label className={labelCls}>Autor</label>
              <input className={inputCls} value={local.author} onChange={(e) => set('author', e.target.value)} />
            </div>

            <div>
              <label className={labelCls}>Tip dokumenta</label>
              <select className={selectCls} value={local.tipDokumentaId} onChange={(e) => setTipDokumenta(e.target.value)}>
                <option value="">Svi tipovi</option>
                {tipoviDokumenta.map((t) => (
                  <option key={t.id} value={t.id}>{t.naziv}</option>
                ))}
              </select>
            </div>
            <div>
              <label className={labelCls}>Tag</label>
              <input className={inputCls} value={local.tag} onChange={(e) => set('tag', e.target.value)} />
            </div>

            <div>
              <label className={labelCls}>Datum od</label>
              <input type="date" className={inputCls} value={local.dateFrom} onChange={(e) => set('dateFrom', e.target.value)} />
            </div>
            <div>
              <label className={labelCls}>Datum do</label>
              <input type="date" className={inputCls} value={local.dateTo} onChange={(e) => set('dateTo', e.target.value)} />
            </div>

            <div className="col-span-2">
              <label className={labelCls}>Projekat</label>
              <select className={selectCls} value={local.projektId} onChange={(e) => set('projektId', e.target.value)}>
                <option value=""></option>
                {projects.map((p) => (
                  <option key={p.id ?? p.name} value={p.id ?? ''}>{p.name}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Metapodaci */}
          {availableMetadata.length > 0 && (
            <div className="rounded-lg border border-hairline bg-surface-2 p-4">
              <div className="mb-3 text-sm font-semibold text-ink">Pretraga po metapodacima</div>
              <div className="space-y-3">
                {availableMetadata.map((item) => {
                  const row = local.metadataFilters.find((r) => r.tipMetapodatkaId === item.id)
                  return (
                    <div key={item.id} className="grid gap-1">
                      <label className={labelCls}>
                        {item.naziv} <span className="text-ink-tertiary">({item.tipPodatka})</span>
                      </label>
                      <input
                        className={inputCls}
                        value={row?.vrednost ?? ''}
                        onChange={(e) => setMetadataField(item.id, e.target.value)}
                      />
                    </div>
                  )
                })}
              </div>
            </div>
          )}
        </div>

        {/* Akcije */}
        <div className="flex justify-end gap-2 border-t border-hairline px-6 py-4 shrink-0">
          <Button variant="secondary" onClick={() => setLocal(EMPTY_ADVANCED_FILTERS)}>Poništi</Button>
          <Button variant="primary" onClick={() => onApply(local)}>Pretraži</Button>
        </div>
      </div>
    </div>
  )
}
