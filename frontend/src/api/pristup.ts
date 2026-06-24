import { apiFetch } from './client'
import { getAuthToken } from '../auth/authStorage'
import type { NivoPrava, PravaPristupa } from '../types/pravaPristupa'

const AUTH_BASE = import.meta.env.VITE_AUTH_API_BASE_URL ?? '/auth-api'

export interface KorisnikInfo {
  id: number
  name: string
  surname: string
  email: string
  role: string
  uuid?: string
}

export async function fetchAllUsers(): Promise<KorisnikInfo[]> {
  const token = getAuthToken()
  const res = await fetch(`${AUTH_BASE}/users`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })
  if (!res.ok) throw new Error(`Greška pri učitavanju korisnika (${res.status})`)
  return res.json() as Promise<KorisnikInfo[]>
}

export function getDokumentAccess(dokumentId: string): Promise<PravaPristupa[]> {
  return apiFetch<PravaPristupa[]>(`/prava-pristupa/dokument/${dokumentId}`)
}

export function getProjekatAccess(projekatId: string): Promise<PravaPristupa[]> {
  return apiFetch<PravaPristupa[]>(`/prava-pristupa/projekat/${projekatId}`)
}

export function grantDokumentAccess(
  dokumentId: string,
  korisnikId: string,
  nivo: NivoPrava,
  dodeljivaoId: string,
): Promise<PravaPristupa> {
  return apiFetch<PravaPristupa>('/prava-pristupa/grant/dokument', {
    method: 'POST',
    body: JSON.stringify({ dokumentId, korisnikId, nivo, dodeljivaoId }),
  })
}

export function grantProjekatAccess(
  projekatId: string,
  korisnikId: string,
  nivo: NivoPrava,
  dodeljivaoId: string,
): Promise<PravaPristupa> {
  return apiFetch<PravaPristupa>('/prava-pristupa/grant/projekat', {
    method: 'POST',
    body: JSON.stringify({ projekatId, korisnikId, nivo, dodeljivaoId }),
  })
}

export function revokeDokumentAccess(dokumentId: string, korisnikId: string): Promise<void> {
  return apiFetch<void>('/prava-pristupa/revoke/dokument', {
    method: 'DELETE',
    body: JSON.stringify({ dokumentId, korisnikId }),
  })
}

export function revokeProjekatAccess(projekatId: string, korisnikId: string): Promise<void> {
  return apiFetch<void>('/prava-pristupa/revoke/projekat', {
    method: 'DELETE',
    body: JSON.stringify({ projekatId, korisnikId }),
  })
}

export function checkAccess(korisnikId: string, dokumentId: string): Promise<{ nivo: string }> {
  return apiFetch<{ nivo: string }>(
    `/prava-pristupa/check?korisnikId=${korisnikId}&dokumentId=${dokumentId}`,
  )
}

export function fetchProjekatPristupIds(korisnikId: string, projekatIds: string[]): Promise<string[]> {
  const params = new URLSearchParams({ korisnikId })
  for (const id of projekatIds) params.append('projekatIds', id)
  return apiFetch<string[]>(`/prava-pristupa/projekti-pristup?${params.toString()}`)
}
