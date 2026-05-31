import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { TextArea } from '../../components/ui/TextArea'
import { Button } from '../../components/ui/Button'
import { updateSectionInput } from '../../api/smartDocs' 
import type { SmartDocument } from '../../types/smartDocs'
import { fetchDocumentById } from '../../api/smartDocs'


export function DocumentEditorPage() {
  const { docId } = useParams<{ docId: string }>()
  const navigate = useNavigate()
  const [document, setDocument] = useState<SmartDocument | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (docId) {
    fetchDocumentById(docId)
      .then(setDocument)
      .catch(err => console.error("Greška:", err));
  }
}, [docId])

  const handleUpdateSection = async (sectionId: number, text: string) => {
    setSaving(true);
    try {
      await updateSectionInput(sectionId, text)
      if (document) {
        const newSections = document.sections.map(s => 
          s.id === sectionId ? { ...s, userInput: text } : s
        )
        setDocument({ ...document, sections: newSections })
      }
    } catch (err) {
       console.error("Greška pri čuvanju sekcije:", err);
    } finally {
      setSaving(false);
    }
  }

  if (!document) return <AppShell><div>Učitavanje...</div></AppShell>

  return (
    <AppShell>
      <div className="mx-auto max-w-4xl">
        <header className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-ink">Popunjavanje dokumentacije</h1>
            <p className="text-sm text-ink-subtle">ID Dokumenta: {docId}</p>
          </div>
          <Button variant="secondary" onClick={() => navigate('/smart-docs')}>Završi kasnije</Button>
        </header>

        <div className="grid gap-8">
          {document.sections.map((section, index) => (
            <section key={section.id} className="rounded-xl border border-hairline bg-surface-1 p-6">
              <h2 className="mb-4 text-lg font-medium text-primary">
                Sekcija {index + 1}: {/* Ovde bi backend trebao da vrati i title sekcije */}
              </h2>
              
              <TextArea
                label="Vaš unos / Kontekst za AI"
                name={`ana-input-${section.id}`}
                value={section.userInput}
                onChange={(e) => handleUpdateSection(section.id, e.target.value)}
                placeholder="Unesite specifične podatke za ovu sekciju..."
                className="min-h-[150px]"
              />
              
              <div className="mt-4 flex justify-end">
                <Button variant="tertiary" disabled>Generiši tekst (Uskoro)</Button>
              </div>
            </section>
          ))}
        </div>

        <footer className="mt-12 border-t border-hairline pt-6">
          <Button fullWidth onClick={() => navigate('/smart-docs')}
            disabled={saving}>
           {saving ? 'Čuvanje...' : 'Sačuvaj i zatvori'}
          </Button>
        </footer>
      </div>
    </AppShell>
  )
}