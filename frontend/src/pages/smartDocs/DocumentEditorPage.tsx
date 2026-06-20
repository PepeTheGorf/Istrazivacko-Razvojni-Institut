import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { TextArea } from '../../components/ui/TextArea'
import { Button } from '../../components/ui/Button'
import { updateSectionInput, generateSectionContent, completeDocument, saveSectionFeedback } from '../../api/smartDocs' 
import type { SmartDocument } from '../../types/smartDocs'
import { apiFetch } from '../../api/client'
import { Breadcrumbs } from '../../components/ui/Breadcrumbs'
import { ProgressBar } from '../../components/ui/ProgressBar'

export function DocumentEditorPage() {
  const { docId } = useParams<{ docId: string }>()
  const navigate = useNavigate()
  const [document, setDocument] = useState<SmartDocument | null>(null)
  const [saving, setSaving] = useState(false)
  const [generatingId, setGeneratingId] = useState<number | null>(null)

  useEffect(() => {
    const loadDoc = async () => {
      if (docId) {
        try {
          const data = await apiFetch<SmartDocument>(`/smart-docs/documents/${docId}`)
          setDocument(data)
        } catch (err) {
          console.error("Greška pri učitavanju dokumenta:", err)
        }
      }
    }
    void loadDoc()
  }, [docId])

  const handleUpdateSection = async (sectionId: number, text: string) => {
    setSaving(true)
    try {
      await updateSectionInput(sectionId, text)
      if (document) {
        const newSections = document.sections.map(s => 
          s.id === sectionId ? { ...s, userInput: text } : s
        )
        setDocument({ ...document, sections: newSections })
      }
    } catch {
       console.error("Greška pri čuvanju sekcije")
    } finally {
      setSaving(false)
    }
  }

  const handleGenerate = async (sectionId: number) => {
    setGeneratingId(sectionId)
    try {
      const response = await generateSectionContent(sectionId)
      if (document) {
        const updatedSections = document.sections.map(s => 
          s.id === sectionId ? { ...s, llmResult: response.result } : s
        )
        setDocument({ ...document, sections: updatedSections })
      }
    } catch {
      alert("Nije uspelo generisanje teksta.")
    } finally {
      setGeneratingId(null)
    }
  }

  const handleComplete = async () => {
    if (!window.confirm("Da li ste sigurni da želite da završite? Nakon ovoga nema izmena.")) return
    try {
      await completeDocument(Number(docId))
      navigate('/my-smart-docs')
    } catch {
      alert("Greška pri završavanju dokumenta.")
    }
  }

  const handleSaveFeedback = async (sectionId: number, rating: number, comment: string) => {
    try {
      await saveSectionFeedback(sectionId, rating, comment)
      if (document) {
        const updated = document.sections.map(s => 
          s.id === sectionId ? { ...s, rating, feedbackComment: comment } : s
        )
        setDocument({ ...document, sections: updated })
      }
    } catch {
      alert("Greška pri čuvanju ocene.")
    }
  }

  if (!document) return <AppShell><div>Učitavanje...</div></AppShell>

  const totalSections = document.sections.length
  const generatedSections = document.sections.filter(s => !!s.llmResult).length
  const progress = Math.round((generatedSections / totalSections) * 100)

  return (
    <AppShell>
      <div className="mx-auto max-w-4xl">
        <Breadcrumbs items={[
          { label: 'Moja Dokumentacija', to: '/my-smart-docs' },
          { label: 'Editor' }
        ]} />

        <header className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-ink">Popunjavanje dokumentacije</h1>
            <h2 className="text-lg font-medium text-ink-subtle">{document.name}</h2>
            <p className="text-sm text-ink-subtle">Šablon: {document.templateName}</p>
          </div>
          <div className="flex gap-3">
            <Button variant="secondary" onClick={() => navigate('/my-smart-docs')}>Završi kasnije</Button>
            <Button onClick={handleComplete}>Finalizuj</Button>
          </div>
        </header>

        <div className="mb-8">
          <ProgressBar progress={progress} label="Napredak popunjavanja dokumenta" />
        </div>

        <div className="grid gap-8">
          {document.sections.map((section, index) => (
            <section key={section.id} className="rounded-xl border border-hairline bg-surface-1 p-6">
              <h2 className="mb-4 text-lg font-medium text-primary">
                Sekcija {index + 1}: {section.title}
              </h2>
              
              <TextArea
                label="Vaš unos / Kontekst za AI"
                value={section.userInput}
                onChange={(e) => handleUpdateSection(section.id, e.target.value)}
                placeholder="Unesite podatke za ovu sekciju..."
                className="mb-4 min-h-[150px]"
              />

              {section.llmResult && (
                <div className="space-y-4">
                  <div className="rounded-lg bg-surface-2 p-4 border-l-4 border-primary">
                    <h3 className="text-xs font-semibold uppercase text-ink-subtle mb-2">AI Predlog:</h3>
                    <p className="text-sm text-ink whitespace-pre-wrap">{section.llmResult}</p>
                  </div>
                  
                  {/* FEEDBACK UI */}
                  <div className="rounded-lg bg-surface-2/50 p-4 border border-hairline">
                    <h4 className="text-[10px] font-bold uppercase text-ink-subtle mb-3">Oceni ovaj AI predlog:</h4>
                    <div className="flex items-center gap-4">
                      <div className="flex gap-1">
                        {[1, 2, 3, 4, 5].map(num => (
                          <button 
                            key={num}
                            onClick={() => handleSaveFeedback(section.id, num, section.feedbackComment || '')}
                            className={`h-8 w-8 rounded border text-xs font-bold transition-all ${
                              section.rating === num ? 'bg-primary border-primary text-white' : 'border-hairline text-ink-subtle hover:bg-surface-3'
                            }`}
                          >
                            {num}
                          </button>
                        ))}
                      </div>
                      <input 
                        type="text" 
                        placeholder="Opcioni komentar..." 
                        className="flex-1 bg-surface-3 border border-hairline rounded px-3 py-1.5 text-sm text-ink focus:outline-none focus:border-primary"
                        defaultValue={section.feedbackComment}
                        onBlur={(e) => handleSaveFeedback(section.id, section.rating || 0, e.target.value)}
                      />
                    </div>
                  </div>
                </div>
              )}
              
              <div className="mt-4 flex justify-end">
                <Button 
                  variant="tertiary" 
                  onClick={() => handleGenerate(section.id)}
                  disabled={generatingId === section.id || !section.userInput}
                >
                  {generatingId === section.id ? 'AI razmišlja...' : 'Generiši tekst'}
                </Button>
              </div>
            </section>
          ))}
        </div>
      </div>
    </AppShell>
  )
}