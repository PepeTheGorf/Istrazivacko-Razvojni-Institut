import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { TextInput } from '../../components/ui/TextInput'
import { TextArea } from '../../components/ui/TextArea'
import { Button } from '../../components/ui/Button'
import { CreatableSelect } from '../../components/ui/CreatableSelect' 
import { useTemplateWizard } from '../../hooks/useTemplateWizard'
import { fetchDomains, fetchCategories } from '../../api/smartDocs'
import type { SmartDomain, SmartCategory } from '../../types/smartDocs'
import { cn } from '../../lib/cn'

export function TemplateCreateWizardPage() {
  const navigate = useNavigate()
  const [domains, setDomains] = useState<SmartDomain[]>([])
  const [categories, setCategories] = useState<SmartCategory[]>([])
  const wizard = useTemplateWizard(() => navigate('/smart-templates'))

  useEffect(() => {
    fetchDomains().then(setDomains)
    fetchCategories().then(setCategories)
  }, [])

  const isStep3Valid = 
    wizard.sections.length > 0 && 
    wizard.sections.every(s => s.title.trim() !== '' && s.systemPrompt.trim() !== '')

  const isStep2Valid = (wizard.domainId || wizard.newDomain) && (wizard.categoryId || wizard.newCategory)

  const getStepTitle = () => {
    if (wizard.step === 1) return "Kreirajte vaš novi šablon"
    return wizard.name || "Novi šablon"
  }

  return (
     <AppShell>
      <div className="mx-auto max-w-4xl">
        {/* BREADCRUMBS */}
        <nav className="mb-4 flex items-center gap-2 text-xs font-medium text-ink-subtle uppercase tracking-wider">
          <span className="cursor-pointer hover:text-ink transition-colors" onClick={() => navigate('/smart-templates')}>
            Lista šablona
          </span>
          <span>/</span>
          
          <span 
            className={cn("cursor-pointer transition-colors", wizard.step === 1 ? "text-primary" : "hover:text-ink")}
            onClick={() => wizard.setStep(1)}
          >
            Osnovne informacije
          </span>

          {wizard.step >= 2 && (
            <>
              <span>/</span>
              <span 
                className={cn("cursor-pointer transition-colors", wizard.step === 2 ? "text-primary" : "hover:text-ink")}
                onClick={() => wizard.setStep(2)}
              >
                Oblast i Tip
              </span>
            </>
          )}

          {wizard.step >= 3 && (
            <>
              <span>/</span>
              <span className={cn(wizard.step === 3 ? "text-primary" : "")}>
                Sekcije
              </span>
            </>
          )}
        </nav>

        <header className="mb-8">
          <h1 className="text-3xl font-semibold text-ink leading-tight">{getStepTitle()}</h1>
          {wizard.step > 1 && wizard.description && (
             <p className="text-ink-subtle mt-1 italic text-sm">{wizard.description}</p>
          )}
          <p className="text-primary font-medium mt-2">Korak {wizard.step} od 3</p>
        </header>

        <div className="rounded-xl border border-hairline bg-surface-1 p-6 shadow-sm">
          {/* KORAK 1 */}
          {wizard.step === 1 && (
            <div className="grid gap-6">
              <TextInput label="Naziv šablona:" value={wizard.name} onChange={e => wizard.setName(e.target.value)} placeholder="npr. Tehnički izveštaj" required />
              <TextArea label="Opis (opciono):" name="templateDesc" value={wizard.description} onChange={e => wizard.setDescription(e.target.value)} />
              <Button onClick={() => wizard.setStep(2)} disabled={!wizard.name.trim()}>Sledeći korak</Button>
            </div>
          )}

          {/* KORAK 2 */}
          {wizard.step === 2 && (
            <div className="grid gap-10">
              <div className="space-y-6">
                <CreatableSelect 
                  label="Izaberite ili unesite oblast:"
                  options={domains}
                  value={wizard.domainId || wizard.newDomain}
                  onChange={(val) => {
                    wizard.setDomainId(val.id || 0)
                    wizard.setNewDomain(val.id ? '' : val.name)
                  }}
                />
                <hr className="border-hairline" />
                <CreatableSelect 
                  label="Izaberite ili unesite tip dokumentacije:"
                  options={categories}
                  value={wizard.categoryId || wizard.newCategory}
                  onChange={(val) => {
                    wizard.setCategoryId(val.id || 0)
                    wizard.setNewCategory(val.id ? '' : val.name)
                  }}
                />
              </div>
              <div className="flex gap-4 pt-4">
                <Button variant="secondary" className="flex-1" onClick={() => wizard.setStep(1)}>Nazad</Button>
                <Button className="flex-1" onClick={() => wizard.setStep(3)} disabled={!isStep2Valid}>Dalje - Sekcije</Button>
              </div>
            </div>
          )}

          {/* KORAK 3 */}
          {wizard.step === 3 && (
            <div className="grid gap-6">
              {wizard.sections.length === 0 ? (
                <div className="py-12 text-center border-2 border-dashed border-hairline rounded-lg">
                   <p className="text-ink-subtle mb-4">Vaš šablon još uvek nema sekcije.</p>
                   <Button variant="secondary" onClick={wizard.addSection}>+ Dodaj prvu sekciju</Button>
                </div>
              ) : (
                <>
                  <div className="grid gap-4">
                    {wizard.sections.map((section, index) => (
                      <div key={index} className="relative rounded-lg border border-hairline p-5 bg-surface-2 shadow-sm">
                        <div className="mb-4 flex justify-between items-center">
                          <h3 className="font-semibold text-xs text-primary uppercase tracking-widest">Sekcija #{index + 1}</h3>
                          <Button variant="delete" onClick={() => wizard.removeSection(index)}>Ukloni</Button>
                        </div>
                        <TextInput label="Naslov sekcije" className="mb-4" value={section.title} onChange={e => wizard.updateSection(index, 'title', e.target.value)} required />
                        <TextArea label="Sistemski Prompt (Uputstvo za AI)" name={`prompt-${index}`} value={section.systemPrompt} onChange={e => wizard.updateSection(index, 'systemPrompt', e.target.value)} required />
                      </div>
                    ))}
                  </div>
                  <Button variant="secondary" onClick={wizard.addSection}>+ Dodaj novu sekciju</Button>
                </>
              )}

              {wizard.error && <p className="text-error text-sm font-medium bg-error/10 p-3 rounded-md">{wizard.error}</p>}
              
              <div className="flex gap-4 pt-6 border-t border-hairline">
                <Button variant="secondary" className="flex-1" onClick={() => wizard.setStep(2)}>Nazad</Button>
                <Button 
                  className="flex-1" 
                  onClick={wizard.handleSave} 
                  disabled={wizard.loading || !isStep3Valid} 
                >
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