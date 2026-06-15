import { apiFetch } from './client'
import type { TipMetapodatka } from '../types/tipMetapodatka'

export function fetchTipMetapodataka(): Promise<TipMetapodatka[]> {
  return apiFetch<TipMetapodatka[]>('/tip-metapodatka')
}

export function createTipMetapodatka(input: {
  naziv: string
  tipPodatka: TipMetapodatka['tipPodatka']
  jeObavezan: boolean
  tipDokumentaId: string
}): Promise<TipMetapodatka> {
  return apiFetch<TipMetapodatka>('/tip-metapodatka', {
    method: 'POST',
    body: JSON.stringify(input),
  })
}
