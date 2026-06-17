import { apiFetch } from './client'

export interface SearchDocumentsRequest {
  prompt: string
  similarityThreshold?: number
}

export interface SearchDocumentsResponse {
  documentIds: string[]
  count: number
  suggestedTagName: string
}

export interface AutoTagRequest {
  prompt?: string
  similarityThreshold?: number
  tagNames: string[]
  documentIds: string[]
}

export interface AutoTagResponse {
  appliedTags: string[]
  taggedDocumentCount: number
  createdNewTags: string[]
}

export async function searchDocumentsForTagging(
  request: SearchDocumentsRequest,
): Promise<SearchDocumentsResponse> {
  return apiFetch<SearchDocumentsResponse>('/tags/search-documents', {
    method: 'POST',
    body: JSON.stringify({
      prompt: request.prompt,
      similarityThreshold: request.similarityThreshold ?? 0.30,
    }),
  })
}

export async function autoTagDocuments(request: AutoTagRequest): Promise<AutoTagResponse> {
  return apiFetch<AutoTagResponse>('/tags/auto-tag', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
