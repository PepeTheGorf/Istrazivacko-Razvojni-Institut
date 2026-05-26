import { TextInput } from '../../../components/ui/TextInput'
import type { WorkflowSortBy } from '../hooks/useWorkflowsPage'

interface WorkflowListToolbarProps {
  search: string
  onSearchChange: (value: string) => void
  sortBy: WorkflowSortBy
  onSortByChange: (value: WorkflowSortBy) => void
}

export function WorkflowListToolbar({
  search,
  onSearchChange,
  sortBy,
  onSortByChange,
}: WorkflowListToolbarProps) {
  return (
    <div className="flex flex-wrap items-end gap-4">
      <div className="min-w-[240px] flex-1">
        <TextInput
          label=""
          name="search"
          aria-label="Pretraži postojeće"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Pretraži postojeće..."
          className="[&>span:first-child]:sr-only"
        />
      </div>
      <label className="grid gap-1">
        <span className="text-[13px] font-medium text-ink-muted">Sortiraj Prema</span>
        <select
          value={sortBy}
          onChange={(e) => onSortByChange(e.target.value as WorkflowSortBy)}
          className="min-h-11 min-w-[160px] rounded-md border border-hairline bg-surface-1 px-3 py-2 text-sm text-ink focus:border-hairline-strong focus:outline-2 focus:outline-primary-focus/50"
        >
          <option value="name">Naziv</option>
          <option value="newest">Najnovije</option>
        </select>
      </label>
    </div>
  )
}
