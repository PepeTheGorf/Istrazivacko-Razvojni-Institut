import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { Breadcrumbs } from '../../components/ui/Breadcrumbs'
import { apiFetch } from '../../api/client'
import type { SmartTemplate } from '../../types/smartDocs'
import { PromptManagementModal } from '../../components/smartDocs/PromptManagementModal'

export function TemplateDetailsPage() {
  const { templateId } = useParams()
  const navigate = useNavigate()
  const [template, setTemplate] = useState<SmartTemplate | null>(null)
  
  // Stanje za modal
  const [selectedSection, setSelectedSection] = useState<{id: number, title: string} | null>(null)

  useEffect(() => {
    apiFetch<SmartTemplate>(`/smart-docs/templates/${templateId}`).then(setTemplate).catch(err=>console.error("Greska:", err))
  }, [templateId])

  if (!template) return <AppShell>Učitavanje...</AppShell>

  return (
    <AppShell>
      <div className="mx-auto max-w-5xl">
        <Breadcrumbs items={[
          { label: 'Šabloni', to: '/smart-templates' },
          { label: template.name }
        ]} />

        <header className="mb-10 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-semibold text-ink">{template.name}</h1>
            <p className="text-ink-subtle">{template.domain.name} / {template.category.name}</p>
          </div>
          <Button variant="secondary" onClick={() => navigate('/smart-templates')}>Nazad</Button>
        </header>

        <div className="grid gap-6">
          <h2 className="text-lg font-medium text-ink">Sekcije i Promptovi</h2>
          {template.sections.map((section, index) => (
            <div key={section.id} className="flex items-center justify-between rounded-xl border border-hairline bg-surface-1 p-6 transition-colors hover:bg-surface-2/50">
              <div className="flex items-center gap-4">
                <span className="flex h-8 w-8 items-center justify-center rounded-full bg-surface-3 text-xs font-bold text-primary">
                  {index + 1}
                </span>
                <div>
                  <h3 className="font-semibold text-ink">{section.title}</h3>
                 
                </div>
              </div>
              <Button 
                variant="secondary" 
                onClick={() => setSelectedSection({ id: section.id!, title: section.title })}
              >
                Upravljaj verzijama
              </Button>
            </div>
          ))}
        </div>

        {/* MODAL POZIV */}
        {selectedSection && (
          <PromptManagementModal 
            isOpen={!!selectedSection}
            onClose={() => setSelectedSection(null)}
            sectionId={selectedSection.id}
            sectionTitle={selectedSection.title}
          />
        )}
      </div>
    </AppShell>
  )
}