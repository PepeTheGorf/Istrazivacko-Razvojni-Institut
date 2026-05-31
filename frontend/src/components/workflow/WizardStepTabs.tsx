import { cn } from '../../lib/cn'
import {
  WIZARD_TABS,
  WIZARD_TAB_LABELS,
  type WizardTabKey,
} from './workflowWizardConfig'

export type { WizardTabKey }

export type WizardTabStatus = 'completed' | 'current' | 'upcoming'

interface WizardStepTabsProps {
  active: WizardTabKey
  getTabStatus: (key: WizardTabKey) => WizardTabStatus
  onSelect?: (key: WizardTabKey) => void
  allowNavigation?: boolean
}

export function WizardStepTabs({
  active,
  getTabStatus,
  onSelect,
  allowNavigation = false,
}: WizardStepTabsProps) {
  return (
    <nav className="border-b border-hairline" aria-label="Koraci čarobnjaka">
      <div className="flex justify-center">
        <div className="flex flex-wrap justify-center gap-8 md:gap-14">
          {WIZARD_TABS.map((key) => {
            const status = getTabStatus(key)
            const isActive = key === active
            const Tag = allowNavigation && onSelect ? 'button' : 'span'
            return (
              <Tag
                key={key}
                type={Tag === 'button' ? 'button' : undefined}
                className={cn(
                  'relative -mb-px px-1 pb-4 text-base font-semibold transition-colors md:text-lg',
                  status === 'current' && 'text-primary',
                  status === 'completed' && 'text-success',
                  status === 'upcoming' && 'text-ink-subtle',
                  allowNavigation && onSelect && status !== 'current' && 'cursor-pointer hover:text-ink',
                  allowNavigation && onSelect && 'border-none bg-transparent',
                )}
                onClick={allowNavigation && onSelect ? () => onSelect(key) : undefined}
              >
                {WIZARD_TAB_LABELS[key]}
                {isActive && (
                  <span className="absolute right-0 bottom-0 left-0 h-0.5 rounded-full bg-primary" />
                )}
                {!isActive && status === 'completed' && (
                  <span className="absolute right-0 bottom-0 left-0 h-0.5 rounded-full bg-success/70" />
                )}
              </Tag>
            )
          })}
        </div>
      </div>
    </nav>
  )
}
