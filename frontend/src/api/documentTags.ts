import { apiFetch } from './client'
import type { DokumentTag } from '../types/documentTag'

export function fetchDocumentTags(dokumentId: string): Promise<DokumentTag[]> {
  return apiFetch<DokumentTag[]>(`/dokument-tag/dokument/${dokumentId}`)
}
