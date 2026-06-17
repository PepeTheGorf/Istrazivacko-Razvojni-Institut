import { apiFetch } from './client'
import type { Tag } from '../types/tag'

export function fetchTags(): Promise<Tag[]> {
  return apiFetch<Tag[]>('/tag')
}

export function createTag(naziv: string): Promise<Tag> {
  return apiFetch<Tag>('/tag', {
    method: 'POST',
    body: JSON.stringify({ naziv }),
  })
}
