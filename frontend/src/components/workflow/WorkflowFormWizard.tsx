import { BasicStep } from '../../pages/workflowCreate/steps/BasicStep'
import { ConditionsStep } from '../../pages/workflowCreate/steps/ConditionsStep'
import { PhasesStep } from '../../pages/workflowCreate/steps/PhasesStep'
import { ReviewStep } from '../../pages/workflowCreate/steps/ReviewStep'
import {
  useWorkflowFormWizard,
  type WorkflowFormInitial,
} from '../../hooks/useWorkflowFormWizard'
import type { ButtonIconName } from '../ui/buttonIcons'
import type { WorkflowCreation } from '../../types/workflow'
import { WorkflowWizardFooter } from './WorkflowWizardFooter'
import { WizardStepTabs } from './WizardStepTabs'
import { WIZARD_TAB_LABELS } from './workflowWizardConfig'

interface WorkflowFormWizardProps {
  title: string
  initial?: WorkflowFormInitial
  submitLabel: string
  submitIcon?: ButtonIconName
  onCancel: () => void
  onSubmit: (workflow: WorkflowCreation) => Promise<void>
}

export function WorkflowFormWizard({
  title,
  initial,
  submitLabel,
  submitIcon,
  onCancel,
  onSubmit,
}: WorkflowFormWizardProps) {
  const wizard = useWorkflowFormWizard({ initial, onCancel, onSubmit })

  return (
    <div className="grid w-full gap-6">
      <header className="grid gap-1">
        <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
          {title}
        </h1>
        <p className="m-0 text-xl font-semibold tracking-tight text-ink-subtle md:text-2xl">
          {WIZARD_TAB_LABELS[wizard.activeTab]}
        </p>
      </header>

      <WizardStepTabs
        active={wizard.activeTab}
        getTabStatus={wizard.getTabStatus}
        allowNavigation={wizard.tabIndex > 0}
        onSelect={wizard.handleTabSelect}
      />

      {wizard.error && (
        <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
          {wizard.error}
        </p>
      )}

      <section className="min-h-[320px] w-full">
        {wizard.activeTab === 'basic' && (
          <BasicStep
            name={wizard.name}
            description={wizard.description}
            onNameChange={wizard.setName}
            onDescriptionChange={wizard.setDescription}
          />
        )}
        {wizard.activeTab === 'phases' && (
          <PhasesStep
            phases={wizard.phases}
            newPhaseName={wizard.newPhaseName}
            onNewPhaseNameChange={wizard.setNewPhaseName}
            onAddPhase={wizard.addPhase}
            onRemovePhase={wizard.removePhase}
            onMovePhase={wizard.movePhase}
          />
        )}
        {wizard.activeTab === 'conditions' && (
          <ConditionsStep
            phases={wizard.normalizedPhases}
            transitionConditions={wizard.transitionConditions}
            onCreateTransition={wizard.createTransitionCondition}
            onAddTypeToRoute={wizard.addTypeToRoute}
            onRemoveTypeFromRoute={wizard.removeTypeFromRoute}
            onRemoveTransitionRoute={wizard.removeTransitionRoute}
            onReplaceTransition={wizard.replaceTransitionCondition}
          />
        )}
        {wizard.activeTab === 'review' && (
          <ReviewStep
            name={wizard.name}
            description={wizard.description}
            phases={wizard.normalizedPhases}
            transitionConditions={wizard.transitionConditions}
          />
        )}
      </section>

      <WorkflowWizardFooter
        onBack={wizard.goBack}
        onNext={wizard.goNext}
        backDisabled={wizard.saving}
        nextLabel={
          wizard.activeTab === 'review'
            ? wizard.saving
              ? 'Čuvanje…'
              : submitLabel
            : wizard.nextLabel
        }
        nextDisabled={wizard.saving}
        nextIcon={wizard.activeTab === 'review' && !wizard.saving ? submitIcon : undefined}
      />
    </div>
  )
}
