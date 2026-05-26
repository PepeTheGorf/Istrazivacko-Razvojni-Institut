import { Button } from '../ui/Button'
import type { ButtonIconName } from '../ui/buttonIcons'

interface WorkflowWizardFooterProps {
  onBack: () => void
  onNext: () => void
  backDisabled?: boolean
  nextLabel: string
  nextDisabled?: boolean
  nextIcon?: ButtonIconName
}

export function WorkflowWizardFooter({
  onBack,
  onNext,
  backDisabled,
  nextLabel,
  nextDisabled,
  nextIcon,
}: WorkflowWizardFooterProps) {
  return (
    <footer className="flex flex-wrap items-center justify-between gap-3 border-t border-hairline pt-6">
      <Button variant="secondary" type="button" disabled={backDisabled} onClick={onBack}>
        Nazad
      </Button>
      <Button type="button" disabled={nextDisabled} icon={nextIcon} onClick={onNext}>
        {nextLabel}
      </Button>
    </footer>
  )
}
