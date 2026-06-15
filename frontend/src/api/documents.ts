import { apiFetch } from './client'
import type { Dokument } from '../types/document'

export function fetchDocuments(): Promise<Dokument[]> {
  return apiFetch<Dokument[]>('/v1/dokumenti')
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

export default {}
