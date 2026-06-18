import type { TeamMember } from '../types/user'

export function teamMemberLabel(member: Pick<TeamMember, 'firstName' | 'lastName'>): string {
  return `${member.firstName} ${member.lastName}`.trim()
}

export function teamMemberNameById(
  members: TeamMember[],
  memberId: number | undefined,
): string | null {
  if (memberId == null) return null
  const member = members.find((item) => item.id === memberId)
  return member ? teamMemberLabel(member) : `Član #${memberId}`
}
