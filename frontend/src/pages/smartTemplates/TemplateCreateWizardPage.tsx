import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { TextInput } from '../../components/ui/TextInput'
import { TextArea } from '../../components/ui/TextArea'
import { SelectField } from '../../components/ui/SelectField'
import { Button } from '../../components/ui/Button'
import { useTemplateWizard } from '../../hooks/useTemplateWizard'
import { fetchDomains, fetchCategories } from '../../api/smartDocs'
import type { SmartDomain, SmartCategory } from '../../types/smartDocs'

export function TemplateCreateWizardPage() {
  const navigate = useNavigate()
  const [domains, setDomains] = useState<SmartDomain[]>([])
  const [categories, setCategories] = useState<SmartCategory[]>([])
  const wizard = useTemplateWizard(() => navigate('/smart-templates'))

  useEffect(() => {
    fetchDomains().then(setDomains)
    fetchCategories().then(setCategories)
  }, [])

  return (
    <AppShell>
      <div className="mx-auto max-w-4xl">
        <header className="mb-8">
          <h1 className="text-3xl font-semibold text-ink">Novi Pametni Šablon</h1>
          <p className="text-ink-subtle">Korak {wizard.step} od 3</p>
        </header>

        <div className="rounded-xl border border-hairline bg-surface-1 p-6">
          {/* KORAK 1: Osnovno */}
          {wizard.step === 1 && (
            <div className="grid gap-6">
              <TextInput label="Naziv šablona" value={wizard.name} onChange={e => wizard.setName(e.target.value)} required />
              <TextArea label="Opis (opciono)" name="templateName" value={wizard.description} onChange={e => wizard.setDescription(e.target.value)} />
              <Button onClick={() => wizard.setStep(2)}>Dalje - Oblast i Tip</Button>
            </div>
          )}

          {wizard.step === 2 && (
            <div className="grid gap-8">
              <section className="grid gap-4">
                <SelectField label="Izaberi Oblast" value={wizard.domainId} onChange={e => wizard.setDomainId(Number(e.target.value))}>
                  <option value="">-- Nova oblast --</option>
                  {domains.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </SelectField>
                {!wizard.domainId && (
                  <TextInput label="Naziv nove oblasti" value={wizard.newDomain} onChange={e => wizard.setNewDomain(e.target.value)} placeholder="npr. Medicina" />
                )}
              </section>

              <section className="grid gap-4">
                <SelectField label="Izaberi Tip dokumentacije" value={wizard.categoryId} onChange={e => wizard.setCategoryId(Number(e.target.value))}>
                  <option value="">-- Novi tip --</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </SelectField>
                {!wizard.categoryId && (
                  <TextInput label="Naziv novog tipa" value={wizard.newCategory} onChange={e => wizard.setNewCategory(e.target.value)} placeholder="npr. Laboratorijski izveštaj" />
                )}
              </section>

              <div className="flex gap-4">
                <Button variant="secondary" onClick={() => wizard.setStep(1)}>Nazad</Button>
                <Button onClick={() => wizard.setStep(3)}>Dalje - Sekcije</Button>
              </div>
            </div>
          )}

          {/* Sekcije i Promptovi */}
          {wizard.step === 3 && (
            <div className="grid gap-6">
              {wizard.sections.map((section, index) => (
                <div key={index} className="relative rounded-lg border border-hairline p-4 bg-surface-2">
                  <div className="mb-4 flex justify-between items-center">
                    <h3 className="font-medium text-sm">Sekcija #{index + 1}</h3>
                    {wizard.sections.length > 1 && (
                      <Button variant="delete" onClick={() => wizard.removeSection(index)}>Ukloni</Button>
                    )}
                  </div>
                  <TextInput label="Naslov sekcije (npr. Uvod)" className="mb-4" value={section.title} onChange={e => wizard.updateSection(index, 'title', e.target.value)} />
                  <TextArea label="Sistemski Prompt " name="description" value={section.systemPrompt} onChange={e => wizard.updateSection(index, 'systemPrompt', e.target.value)} placeholder="npr. Piši u tonu naučnog rada..." />
                </div>
              ))}
              
              <Button variant="secondary" onClick={wizard.addSection}>+ Dodaj sekciju</Button>

              {wizard.error && <p className="text-error text-sm mt-2">{wizard.error}</p>}
              
              <div className="flex gap-4 pt-4 border-t border-hairline">
                <Button variant="secondary" onClick={() => wizard.setStep(2)}>Nazad</Button>
                <Button onClick={wizard.handleSave} disabled={wizard.loading}>
                  {wizard.loading ? 'Čuvanje...' : 'Sačuvaj šablon'}
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}