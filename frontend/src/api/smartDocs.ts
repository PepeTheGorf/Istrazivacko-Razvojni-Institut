import { apiFetch } from './client'
import type { 
  SmartDomain, 
  SmartCategory, 
  SmartTemplate, 
  TemplateCreationPayload,
  SmartDocument 
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