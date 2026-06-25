import { apiFetch } from './client'
import type { DokumentVerzija, DokumentVerzijaFull } from '../types/verzija'

export function getVerzije(dokumentId: string, korisnikId?: string): Promise<DokumentVerzija[]> {
  const query = korisnikId ? `?korisnikId=${korisnikId}` : ''
  return apiFetch<{ verzije: DokumentVerzija[] }>(`/v1/dokumenti/${dokumentId}/verzije${query}`)
    .then((res) => res.verzije)
}

export function getVerzija(dokumentId: string, verzijaId: string, korisnikId?: string): Promise<DokumentVerzijaFull> {
  const query = korisnikId ? `?korisnikId=${korisnikId}` : ''
  return apiFetch<DokumentVerzijaFull>(`/v1/dokumenti/${dokumentId}/verzije/${verzijaId}${query}`)
}

export function restoreVerzija(dokumentId: string, verzijaId: string, korisnikId: string): Promise<void> {
  return apiFetch<void>(`/v1/dokumenti/${dokumentId}/verzije/${verzijaId}/restore`, {
    method: 'POST',
    body: JSON.stringify({ korisnikId }),
  })
}
