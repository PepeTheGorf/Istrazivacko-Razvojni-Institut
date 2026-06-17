import { apiFetch } from './client'
import type { DokumentTag } from '../types/documentTag'

export function fetchDocumentTags(dokumentId: string): Promise<DokumentTag[]> {
  return apiFetch<DokumentTag[]>(`/dokument-tag/dokument/${dokumentId}`)
}

export function createDokumentTag(dokumentId: string, tagId: string): Promise<DokumentTag> {
  return apiFetch<DokumentTag>('/dokument-tag', {
    method: 'POST',
    body: JSON.stringify({ dokumentId, tagId }),
  })
}

export function deleteDokumentTag(dokumentId: string, tagId: string): Promise<void> {
  return apiFetch<void>(`/dokument-tag/${dokumentId}/${tagId}`, { method: 'DELETE' })
}
