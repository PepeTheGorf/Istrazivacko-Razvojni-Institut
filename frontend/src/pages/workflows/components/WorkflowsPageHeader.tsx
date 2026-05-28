import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'

export function WorkflowsPageHeader() {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
        Tokovi Rada
      </h1>
      <div className="flex flex-wrap items-center gap-3">
        <Link to="/workflows/new" className="inline-flex">
          <Button type="button" icon="add">
            Novi Tok Rada
          </Button>
        </Link>
      </div>
    </header>
  )
}
