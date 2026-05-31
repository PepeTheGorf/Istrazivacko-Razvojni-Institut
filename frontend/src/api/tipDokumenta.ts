import { apiFetch } from './client'
import type { TipDokumenta } from '../types/tipDokumenta'

export function fetchTipDokumenta(): Promise<TipDokumenta[]> {
  return apiFetch<TipDokumenta[]>('/tip-dokumenta')
}

export function createTipDokumenta(naziv: string): Promise<TipDokumenta> {
  return apiFetch<TipDokumenta>('/tip-dokumenta', {
    method: 'POST',
    body: JSON.stringify({ naziv }),
  })
}
