import { useCallback, useEffect, useState } from 'react'
import { fetchTeamMembers } from '../../../api/users'
import type { TeamMember } from '../../../types/user'

export function useTeamMembers(enabled = true) {
  const [teamMembers, setTeamMembers] = useState<TeamMember[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!enabled) {
      setTeamMembers([])
      return
    }
    setLoading(true)
    setError(null)
    try {
      const members = await fetchTeamMembers()
      setTeamMembers(members)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Učitavanje članova tima nije uspelo')
      setTeamMembers([])
    } finally {
      setLoading(false)
    }
  }, [enabled])

  useEffect(() => {
    void load()
  }, [load])

  return { teamMembers, loading, error, reload: load }
}
