import { apiFetch } from './client'
import type { 
  SmartDomain, 
  SmartCategory, 
  SmartTemplate, 
  TemplateCreationPayload,
  SmartDocument, 
  SmartDocumentSummary
} from '../types/smartDocs'

export function fetchDomains(): Promise<SmartDomain[]> {
  return apiFetch<SmartDomain[]>('/smart-docs/domains')
}

export function fetchCategories(): Promise<SmartCategory[]> {
  return apiFetch<SmartCategory[]>('/smart-docs/categories')
}

export function createSmartTemplate(payload: TemplateCreationPayload): Promise<void> {
  return apiFetch<void>('/smart-docs/templates', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function fetchTemplates(domainId: number, categoryId: number): Promise<SmartTemplate[]> {
  return apiFetch<SmartTemplate[]>(`/smart-docs/templates?domainId=${domainId}&categoryId=${categoryId}`)
}

export function createDocument(templateId: number): Promise<SmartDocument> {
  return apiFetch<SmartDocument>('/smart-docs/documents', {
    method: 'POST',
    body: JSON.stringify({ templateId }),
  })
}

export function updateSectionInput(sectionId: number, userInput: string): Promise<void> {
  return apiFetch<void>(`/smart-docs/sections/${sectionId}`, {
    method: 'PUT',
    body: JSON.stringify({ userInput }),
  })
}

export function fetchDocumentById(docId: string): Promise<SmartDocument> {
  return apiFetch<SmartDocument>(`/smart-docs/documents/${docId}`)
}
export function fetchMySmartDocuments(): Promise<SmartDocumentSummary[]> {
  return apiFetch<SmartDocumentSummary[]>('/smart-docs/my')
}

export function generateSectionContent(sectionId: number): Promise<{ result: string }> {
  return apiFetch<{ result: string }>(`/smart-docs/sections/${sectionId}/generate`, {
    method: 'POST',
  })
}

export function completeDocument(docId: number): Promise<void> {
  return apiFetch<void>(`/smart-docs/documents/${docId}/complete`, {
    method: 'POST',
  })
}

export function saveSectionFeedback(
  sectionId: number, 
  rating: number, 
  comment: string
): Promise<void> {
  return apiFetch<void>(`/smart-docs/sections/${sectionId}/feedback`, {
    method: 'POST',
    body: JSON.stringify({ rating, comment }),
  })
}

export function deleteDocument(docId: number): Promise<void> {
  return apiFetch<void>(`/smart-docs/documents/${docId}`, {
    method: 'DELETE',
  })
}
export interface PromptVersion {
  id: number
  content: string
  versionNumber: number
  active: boolean
  createdAt: string
}

export function fetchPromptHistory(sectionId: number): Promise<PromptVersion[]> {
  return apiFetch<PromptVersion[]>(`/smart-docs/sections/${sectionId}/prompts`)
}

export function createNewPromptVersion(sectionId: number, content: string): Promise<void> {
  return apiFetch<void>(`/smart-docs/sections/${sectionId}/prompts`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  })
}

export function activatePromptVersion(sectionId: number, versionId: number): Promise<void> {
  return apiFetch<void>(`/smart-docs/sections/${sectionId}/prompts/${versionId}/activate`, {
    method: 'PUT',
  })
}