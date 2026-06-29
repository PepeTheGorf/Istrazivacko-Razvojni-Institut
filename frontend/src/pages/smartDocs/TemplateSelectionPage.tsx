import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { SelectField } from '../../components/ui/SelectField'
import { Button } from '../../components/ui/Button'
import { fetchDomains, fetchCategories, fetchTemplates, createDocument } from '../../api/smartDocs'
import type { SmartDomain, SmartCategory, SmartTemplate } from '../../types/smartDocs'

export function TemplateSelectionPage() {
  const navigate = useNavigate()
  const [domains, setDomains] = useState<SmartDomain[]>([])
  const [categories, setCategories] = useState<SmartCategory[]>([])
  const [templates, setTemplates] = useState<SmartTemplate[]>([])

  const [selectedDomain, setSelectedDomain] = useState<number | ''>('')
  const [selectedCategory, setSelectedCategory] = useState<number | ''>('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchDomains().then(setDomains)
    fetchCategories().then(setCategories)
  }, [])

  useEffect(() => {
     const loadTemplates = async () => {
    if (selectedDomain && selectedCategory) {
      try {
        const data = await fetchTemplates(Number(selectedDomain), Number(selectedCategory));
        setTemplates(data);
      } catch (error) {
        console.error("Greška pri učitavanju šablona:", error);
        setTemplates([]);
      }
    } else {
      setTemplates([]);
    }
  };

  void loadTemplates();
}, [selectedDomain, selectedCategory])

  const handleStartDocument = async (templateId: number) => {
    setLoading(true)
    try {
      const newDoc = await createDocument(templateId)
      navigate(`/smart-docs/${newDoc.id}`)
    } catch (err) {
        console.error("Greška pri formiranju dokumenta:", err);
        alert(err instanceof Error ? err.message : 'Greška pri kreiranju dokumenta');
    } finally {
      setLoading(false)
    }
  };

  return (
    <AppShell>
      <div className="mx-auto max-w-4xl">
        <header className="mb-8">
          <h1 className="text-3xl font-semibold text-ink">Kreiraj novu dokumentaciju</h1>
          <p className="text-ink-subtle">Izaberite oblast i tip da biste videli dostupne šablone</p>
        </header>

        <section className="grid gap-6 rounded-xl border border-hairline bg-surface-1 p-6 md:grid-cols-2">
          <SelectField 
            label="1. Izaberi Oblast" 
            name="domainFilter"
            value={selectedDomain} 
            onChange={e => setSelectedDomain(e.target.value ? Number(e.target.value) : '')}
          >
            <option value="">Sve oblasti...</option>
            {domains.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
          </SelectField>

          <SelectField 
            label="2. Izaberi Tip" 
            name="categoryFilter"
            value={selectedCategory} 
            onChange={e => setSelectedCategory(e.target.value ? Number(e.target.value) : '')}
            disabled={!selectedDomain}
          >
            <option value="">Svi tipovi...</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </SelectField>
        </section>

        <section className="mt-8 grid gap-4">
          <h2 className="text-lg font-medium text-ink">Dostupni šabloni ({templates.length})</h2>
          {templates.length > 0 ? (
            <div className="grid gap-4 sm:grid-cols-2">
              {templates.map(t => (
                <div key={t.id} className="flex flex-col justify-between rounded-lg border border-hairline bg-surface-2 p-5 transition-colors hover:bg-surface-3">
                  <div>
                    <h3 className="font-semibold text-ink">{t.name}</h3>
                    <p className="mt-1 text-sm text-ink-subtle">{t.description || 'Nema opisa.'}</p>
                  </div>
                  <Button 
                    className="mt-4" 
                    variant="secondary" 
                    onClick={() => handleStartDocument(t.id)}
                    disabled={loading}
                  >
                    Izaberi ovaj šablon
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-ink-subtle italic">
              {selectedDomain && selectedCategory 
                ? "Nema pronađenih šablona za ovu kombinaciju." 
                : "Molimo izaberite oblast i tip iznad."}
            </p>
          )}
        </section>
      </div>
    </AppShell>
  )
}