import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'

interface ProjectsPageHeaderProps {
  canManage: boolean
}

export function ProjectsPageHeader({ canManage }: ProjectsPageHeaderProps) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <p className="m-0 mb-2 text-[13px] font-medium tracking-wide text-ink-subtle uppercase">
          Projekti
        </p>
        <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
          Projekti
        </h1>
        <p className="mt-2 text-sm text-ink-subtle">
          Pregled i upravljanje projektima u okviru instituta.
        </p>
      </div>

      {canManage ? (
        <div className="flex flex-wrap items-center gap-3">
          <Link to="/projects/new" className="inline-flex">
            <Button type="button" icon="add">
              Novi projekat
            </Button>
          </Link>
        </div>
      ) : null}
    </header>
  )
}

