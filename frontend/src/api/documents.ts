import { apiFetch } from './client'
import { getAuthToken } from '../auth/authStorage'
import type { Dokument } from '../types/document'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api'

export function fetchDocuments(korisnikId?: string | number): Promise<Dokument[]> {
  const query = korisnikId != null ? `?korisnikId=${korisnikId}` : ''
  return apiFetch<Dokument[]>(`/v1/dokumenti${query}`)
}

export function fetchDocumentsByProject(projekatId: string): Promise<Dokument[]> {
  return apiFetch<Dokument[]>(`/v1/dokumenti?projektId=${projekatId}`)
}

export function fetchDocumentById(id: string): Promise<Dokument> {
  return apiFetch<Dokument>(`/v1/dokumenti/${id}`)
}

export function createDocument(payload: any): Promise<Dokument> {
  return apiFetch<Dokument>('/v1/dokumenti', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateDocument(id: string, payload: any): Promise<Dokument> {
  return apiFetch<Dokument>(`/v1/dokumenti/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteDocument(id: string): Promise<void> {
  return apiFetch<void>(`/v1/dokumenti/${id}`, { method: 'DELETE' })
}

export async function uploadDocument(params: {
  file: File
  naziv?: string
  tipDokumentaId?: string
  projektId?: string
  projectName?: string
  authorId?: string
  authorName?: string
  metapodaci?: { tipMetapodatkaId: string; vrednost: string }[]
}): Promise<Dokument> {
  const form = new FormData()
  form.append('file', params.file)
  if (params.naziv) form.append('naziv', params.naziv)
  if (params.tipDokumentaId) form.append('tipDokumentaId', params.tipDokumentaId)
  if (params.projektId) form.append('projektId', params.projektId)
  if (params.projectName) form.append('projectName', params.projectName)
  if (params.authorId) form.append('authorId', params.authorId)
  if (params.authorName) form.append('authorName', params.authorName)
  if (params.metapodaci?.length) form.append('metapodaci', JSON.stringify(params.metapodaci))

  const token = getAuthToken()
  const response = await fetch(`${API_BASE}/v1/dokumenti/upload`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form,
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Upload failed (${response.status})`)
  }

  return response.json() as Promise<Dokument>
}

export default {}
