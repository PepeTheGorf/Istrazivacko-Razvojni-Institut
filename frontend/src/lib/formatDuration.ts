export function formatDurationSeconds(seconds: number): string {
  if (!Number.isFinite(seconds) || seconds <= 0) {
    return '-'
  }

  if (seconds < 60) {
    return `${Math.round(seconds)} s`
  }

  if (seconds < 3600) {
    return `${Math.round(seconds / 60)} min`
  }

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.round((seconds % 3600) / 60)
  return minutes > 0 ? `${hours} h ${minutes} min` : `${hours} h`
}

export function formatDurationHours(seconds: number): string {
  if (!Number.isFinite(seconds) || seconds <= 0) {
    return '-'
  }

  return `${(seconds / 3600).toFixed(2)} h`
}
