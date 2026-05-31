import { apiFetch } from './client'
import type { Tag } from '../types/tag'

export function fetchTags(): Promise<Tag[]> {
  return apiFetch<Tag[]>('/tag')
}
