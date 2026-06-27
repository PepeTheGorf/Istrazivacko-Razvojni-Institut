import { getAuthToken } from '../auth/authStorage'

const ANALYTICS_BASE = import.meta.env.VITE_ANALYTICS_API_BASE_URL ?? 'http://localhost:8082'

async function analyticsFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getAuthToken()
  const res = await fetch(`${ANALYTICS_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init?.headers,
    },
    ...init,
  })
  if (!res.ok) {
    const message = await res.text()
    throw new Error(message || `Request failed (${res.status})`)
  }
  const text = await res.text()
  if (!text) return undefined as T
  return JSON.parse(text) as T
}

export interface DocumentAccessEntry {
  user_id: string
  document_id: string
  project_id: string
  action_type: string
  session_duration_sec: number
  file_size_bytes: number
  created: string
}

export interface TopEntry {
  documentId?: string
  userId?: string
  accessCount: number
}

export interface OverallReport {
  from: string
  to: string
  totalAccesses: number
  uniqueUsers: number
  uniqueDocuments: number
  accessList: DocumentAccessEntry[]
  topDocuments: TopEntry[]
  topUsers: TopEntry[]
}

export interface DocumentReport {
  documentId: string
  from: string
  to: string
  totalAccesses: number
  uniqueUsers: number
  accesses: DocumentAccessEntry[]
  accessByUser: TopEntry[]
}

export interface UserReport {
  userId: string
  from: string
  to: string
  totalAccesses: number
  uniqueDocuments: number
  accesses: DocumentAccessEntry[]
  accessByDocument: TopEntry[]
}

export function getReport(from: string, to: string): Promise<OverallReport> {
  return analyticsFetch<OverallReport>(
    `/document-access.json/report?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
  )
}

export function getReportByDocument(documentId: string, from: string, to: string): Promise<DocumentReport> {
  return analyticsFetch<DocumentReport>(
    `/document-access.json/report/by-document?documentId=${encodeURIComponent(documentId)}&from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
  )
}

export function getReportByUser(userId: string, from: string, to: string): Promise<UserReport> {
  return analyticsFetch<UserReport>(
    `/document-access.json/report/by-user?userId=${encodeURIComponent(userId)}&from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
  )
}

export function getActiveUsers(from: string, to: string): Promise<TopEntry[]> {
  return analyticsFetch<TopEntry[]>(
    `/document-access.json/stats/active-users?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
  )
}

export function getActiveDocuments(from: string, to: string): Promise<TopEntry[]> {
  return analyticsFetch<TopEntry[]>(
    `/document-access.json/stats/active-documents?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
  )
}

function encodeNameMap(map: Record<string, string>): string {
  return Object.entries(map).map(([k, v]) => `${k}|${v}`).join(',')
}

async function downloadPdf(path: string, filename: string) {
  const token = getAuthToken()
  const res = await fetch(`${ANALYTICS_BASE}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!res.ok) throw new Error(`PDF download failed (${res.status})`)
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

export function downloadOverallReportPdf(
  from: string,
  to: string,
  docNames: Record<string, string>,
  userNames: Record<string, string>,
) {
  const params = new URLSearchParams({
    from, to,
    docNames: encodeNameMap(docNames),
    userNames: encodeNameMap(userNames),
  })
  return downloadPdf(`/document-access.json/report/pdf?${params}`, 'izvestaj-pristupa.pdf')
}

export function downloadDocumentReportPdf(
  documentId: string,
  documentName: string,
  from: string,
  to: string,
  userNames: Record<string, string>,
) {
  const params = new URLSearchParams({
    documentId, from, to,
    documentName,
    userNames: encodeNameMap(userNames),
  })
  return downloadPdf(`/document-access.json/report/by-document/pdf?${params}`, `izvestaj-${documentName}.pdf`)
}

export function downloadUserReportPdf(
  userId: string,
  userName: string,
  from: string,
  to: string,
  docNames: Record<string, string>,
) {
  const params = new URLSearchParams({
    userId, from, to,
    userName,
    docNames: encodeNameMap(docNames),
  })
  return downloadPdf(`/document-access.json/report/by-user/pdf?${params}`, `izvestaj-${userName}.pdf`)
}
