import { apiFetch } from './client'

export interface VectorDocumentSearchResult {
    id: string | number
    title?: string
    author_id?: string
    doc_type_id?: string
    project_id?: string
    folder_id?: string | null
    content?: string
    tags?: string[]
    metadata?: Record<string, string>
    created_at?: string
    updated_at?: string
    is_archived?: boolean
    score?: number
    semantic_score?: number
    lexical_score?: number
}

export interface VectorDocumentSearchResponse {
    count: number
    results: VectorDocumentSearchResult[]
}

export interface SearchDocumentsInput {
    query: string
    topK?: number
    docTypeId?: string
    authorId?: string
    projectId?: string
    folderId?: string
    isArchived?: boolean
}

export function searchDocuments(input: SearchDocumentsInput): Promise<VectorDocumentSearchResponse> {
    const params = new URLSearchParams({
        query: input.query,
        top_k: String(input.topK ?? 50),
    })

    if (input.docTypeId) params.set('doc_type_id', input.docTypeId)
    if (input.authorId) params.set('author_id', input.authorId)
    if (input.projectId) params.set('project_id', input.projectId)
    if (input.folderId) params.set('folder_id', input.folderId)
    if (typeof input.isArchived === 'boolean') params.set('is_archived', String(input.isArchived))

    return apiFetch<VectorDocumentSearchResponse>(`/v1/search/documents?${params.toString()}`)
}