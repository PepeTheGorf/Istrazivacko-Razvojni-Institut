import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { Breadcrumbs } from '../../components/ui/Breadcrumbs'
import { apiFetch } from '../../api/client'
import type { SmartTemplate } from '../../types/smartDocs'

export function TemplateDetailsPage() {
  const { templateId } = useParams()
  const navigate = useNavigate()
  const [template, setTemplate] = useState<SmartTemplate | null>(null)
  
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
          <h2 className="text-lg font-medium text-ink">Sekcije i promptovi</h2>
          {template.sections.map((section, index) => (
             <div key={section.id} 
              className="group flex items-center justify-between rounded-xl border border-hairline bg-surface-1 p-6 transition-all hover:border-primary/50 hover:shadow-md">
             <div className="flex items-center gap-5">
             <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-surface-2 text-sm font-bold text-ink-muted group-hover:bg-primary group-hover:text-white transition-colors">
                 {index + 1}
             </div>
            <div>
        <h3 className="font-semibold text-ink text-lg">{section.title}</h3>
        <p className="text-sm text-ink-subtle">
           Konfigurišite AI prompt i istoriju verzija za ovu sekciju.
        </p>
      </div>
    </div>
    
    <div className="flex items-center gap-4">
      <Button 
        variant="primary"
        className="shadow-sm"
        onClick={() => {
          navigate(`/smart-templates/${templateId}/sections/${section.id}/prompt`);        }}
      >
        Konfiguriši prompt
      </Button>
    </div>
  </div>
))}
        </div>
      </div>
    </AppShell>
  )
}