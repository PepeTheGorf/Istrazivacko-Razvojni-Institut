import { apiFetch } from './client'
import type { Metapodatak } from '../types/metapodatak'

export function fetchMetapodatakByDocument(dokumentId: string): Promise<Metapodatak[]> {
  return apiFetch<Metapodatak[]>(`/metapodatak/dokument/${dokumentId}`)
}
