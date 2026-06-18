import { useMemo, useState } from 'react'
import { Button } from '../../../components/ui/Button'
import { SelectField } from '../../../components/ui/SelectField'
import { teamMemberLabel, teamMemberNameById } from '../../../lib/teamMemberLabel'
import type { TeamMember } from '../../../types/user'

interface TeamMemberAssignPanelProps {
  assignedMemberIds: number[]
  teamMembers: TeamMember[]
  loadingMembers?: boolean
  submitting?: boolean
  embedded?: boolean
  onAssign: (memberId: number) => Promise<void>
}

export function TeamMemberAssignPanel({
  assignedMemberIds,
  teamMembers,
  loadingMembers = false,
  submitting = false,
  embedded = false,
  onAssign,
}: TeamMemberAssignPanelProps) {
  const [selectedMemberId, setSelectedMemberId] = useState('')
  const [error, setError] = useState<string | null>(null)

  const availableMembers = useMemo(
    () => teamMembers.filter((member) => !assignedMemberIds.includes(member.id)),
    [assignedMemberIds, teamMembers],
  )

  async function handleAssign() {
    if (!selectedMemberId) {
      setError('Izaberite člana tima.')
      return
    }
    setError(null)
    try {
      await onAssign(Number(selectedMemberId))
      setSelectedMemberId('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Dodavanje člana tima nije uspelo')
    }
  }

  return (
    <div className={embedded ? '' : 'rounded-md border border-hairline bg-surface-2 p-3'}>
      <div className={embedded ? '' : 'mb-2 flex items-center justify-between gap-3'}>
        {!embedded ? <h3 className="m-0 text-sm font-semibold text-ink">Članovi tima</h3> : null}
      </div>

      {assignedMemberIds.length > 0 ? (
        <ul className="m-0 mb-3 flex list-none flex-wrap gap-2 p-0">
          {assignedMemberIds.map((memberId) => (
            <li
              key={memberId}
              className="rounded-md border border-hairline bg-surface-1 px-3 py-1.5 text-sm text-ink"
            >
              {teamMemberNameById(teamMembers, memberId)}
            </li>
          ))}
        </ul>
      ) : (
        <p className="m-0 mb-3 text-sm text-ink-subtle">Nema dodeljenih članova tima.</p>
      )}

      {error ? (
        <p className="m-0 mb-3 rounded-md border border-error/35 bg-error/10 px-3 py-2 text-sm text-[#ffb4b4]">
          {error}
        </p>
      ) : null}

      <div className="grid gap-3">
        <SelectField
          label="Dodaj člana tima"
          name="teamMemberId"
          disabled={loadingMembers || submitting || availableMembers.length === 0}
          value={selectedMemberId}
          onChange={(event) => setSelectedMemberId(event.target.value)}
        >
          <option value="">
            {loadingMembers
              ? 'Učitavanje članova…'
              : availableMembers.length === 0
                ? 'Nema dostupnih članova'
                : 'Izaberite člana tima'}
          </option>
          {availableMembers.map((member) => (
            <option key={member.id} value={member.id}>
              {teamMemberLabel(member)}
            </option>
          ))}
        </SelectField>
        <Button
          type="button"
          icon="add"
          disabled={loadingMembers || submitting || !selectedMemberId}
          onClick={() => void handleAssign()}
          className="w-full sm:w-auto"
        >
          Dodaj člana tima
        </Button>
      </div>
    </div>
  )
}
