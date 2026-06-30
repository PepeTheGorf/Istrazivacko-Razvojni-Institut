import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { TextArea } from '../../components/ui/TextArea'
import { Button } from '../../components/ui/Button'
import { updateSectionInput, generateSectionContent, completeDocument, saveSectionFeedback, updateRefinedResult } from '../../api/smartDocs' 
import type { SmartDocument } from '../../types/smartDocs'
import { apiFetch } from '../../api/client'
import { Breadcrumbs } from '../../components/ui/Breadcrumbs'
import { ProgressBar } from '../../components/ui/ProgressBar'
import { cn } from '../../lib/cn' 

export function DocumentEditorPage() {
  const { docId } = useParams<{ docId: string }>()
  const navigate = useNavigate()
  const [document, setDocument] = useState<SmartDocument | null>(null)
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
    try {
      await updateSectionInput(sectionId, text)
      setDocument(prev => {
        if (!prev) return null
        return {
          ...prev,
          sections: prev.sections.map(s => s.id === sectionId ? { ...s, userInput: text } : s)
        }
      })
    } catch {
       console.error("Greška pri čuvanju ulaza")
    }
  }

  const handleRefinedChange = async (sectionId: number, text: string) => {
    try {
      await updateRefinedResult(sectionId, text)
      setDocument(prev => {
        if (!prev) return null
        return {
          ...prev,
          sections: prev.sections.map(s => s.id === sectionId ? { ...s, refinedResult: text } : s)
        }
      })
    } catch (err) {
      console.error("Greška pri čuvanju izmjena teksta:", err)
    }
  }

  const handleGenerate = async (sectionId: number) => {
    const section = document?.sections.find(s => s.id === sectionId);
    if (!section) return;

    // Ključno: Čuvamo trenutno stanje editora pre nego što ga AI pregazi
    const textCurrentlyInEditor = section.refinedResult || "";

    setGeneratingId(sectionId);
    try {
      const response = await generateSectionContent(sectionId);
      if (document) {
        setDocument({
          ...document,
          sections: document.sections.map(s => 
            s.id === sectionId ? { 
              ...s, 
              // llmResult sada služi kao historija (prethodna verzija)
              llmResult: textCurrentlyInEditor || s.llmResult, 
              refinedResult: response.result 
            } : s
          )
        })
      }
    } catch {
      alert("Nije uspjelo generisanje teksta.")
    } finally {
      setGeneratingId(null)
    }
  }

  const handleUndo = async (sectionId: number, previousVersion: string) => {
    if (window.confirm("Poništiti vaše izmjene i vratiti na prethodnu verziju?")) {
      await handleRefinedChange(sectionId, previousVersion)
    }
  }

  const handleComplete = async () => {
    if (!window.confirm("Finalizacijom dokumenta onemogućavate dalje izmjene. Nastavi?")) return
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
        setDocument({
          ...document,
          sections: document.sections.map(s => 
            s.id === sectionId ? { ...s, rating, feedbackComment: comment } : s
          )
        })
      }
    } catch {
      alert("Greška pri čuvanju ocjene.")
    }
  }

  if (!document) {
    return <AppShell hideSidebar><div className="p-8 text-center text-ink-subtle">Učitavanje editora...</div></AppShell>
  }

  // Validacija sada gleda refinedResult (ono što je zapravo u dokumentu)
  const allFeedbackProvided = document.sections.every(section => {
    if (!section.refinedResult) return true; 
    const hasRating = section.rating && section.rating > 0;
    const hasComment = section.feedbackComment && section.feedbackComment.trim().length > 0;
    return hasRating && hasComment;
  });

  const totalSections = document.sections.length
  const generatedSections = document.sections.filter(s => !!s.refinedResult).length
  const progress = Math.round((generatedSections / totalSections) * 100)

  return (
    <AppShell hideSidebar>
      <div className="mx-auto max-w-[95%]">
        <Breadcrumbs items={[
          { label: 'Moja dokumentacija', to: '/my-smart-docs' },
          { label: 'Interaktivni editor' }
        ]} />

        <header className="mb-8 flex items-center justify-between border-b border-hairline pb-6">
          <div>
            <h1 className="text-3xl font-bold text-ink">{document.name}</h1>
            <p className="text-sm text-ink-subtle">Šablon: <span className="text-primary font-medium">{document.templateName}</span></p>
          </div>
          <div className="flex gap-3">
            <Button variant="secondary" onClick={() => navigate('/my-smart-docs')}>Sačuvaj i zatvori</Button>
            <Button  
              onClick={handleComplete} 
              disabled={!allFeedbackProvided}
              title={!allFeedbackProvided ? "Molimo ostavite ocjenu i komentar za sve AI generisane sekcije prije finalizacije." : ""}
              className={cn( 
                "shadow-lg transition-all",
                !allFeedbackProvided ? "opacity-40 grayscale cursor-not-allowed" : "shadow-primary/20"
              )}
            >
              Finalizuj dokument
            </Button>
          </div>
        </header>

        <div className="mb-10">
          <ProgressBar progress={progress} label="Ukupan progres dokumenta" />
        </div>

        <div className="grid gap-12">
          {document.sections.map((section, index) => {
            // Izvor istine za prikaz editora je refinedResult
            const hasAiResult = !!section.refinedResult;
            
            // Undo je moguć samo ako u historiji (llmResult) imamo nešto različito od trenutnog
            const canUndo = !!section.llmResult && section.llmResult !== section.refinedResult;
            
            let buttonLabel = "Generiši tekst";
            if (generatingId === section.id) buttonLabel = "AI razmišlja...";
            else if (hasAiResult && canUndo) buttonLabel = "AI dorada (refine)";
            else if (hasAiResult) buttonLabel = "Regeneriši";

            return (
              <section key={section.id} className="rounded-2xl border border-hairline bg-surface-1 p-8 shadow-sm transition-all hover:shadow-md">
                <h2 className="mb-6 text-xl font-bold text-primary flex items-center gap-3">
                  <span className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-white text-xs">
                    {index + 1}
                  </span>
                  {section.title}
                </h2>
                
                <div className="grid gap-8 lg:grid-cols-2">
                  <div className="space-y-4">
                    <TextArea
                      name={`input-${section.id}`}
                      label="Vaš unos / Činjenice za AI"
                      value={section.userInput}
                      onChange={(e) => handleUpdateSection(section.id, e.target.value)}
                      placeholder="Unesite ključne podatke ili teze koje AI treba da obradi..."
                      className="min-h-[250px] bg-surface-2/40 focus:bg-surface-1"
                    />
                    <Button 
                      variant={hasAiResult ? "tertiary" : "primary"}
                      onClick={() => handleGenerate(section.id)}
                      disabled={generatingId === section.id || !section.userInput.trim()}
                      className="w-full"
                    >
                      {buttonLabel}
                    </Button>
                  </div>

                  <div className="relative flex flex-col space-y-4">
                    {hasAiResult ? (
                      <>
                        <div className="flex items-center justify-between">
                          <span className="text-[10px] font-bold uppercase tracking-widest text-ink-subtle">
                            AI Predlog (Uredi tekst ispod)
                          </span>
                          {/* Dugme se vidi samo ako canUndo (postoji prethodna verzija) */}
                          {canUndo && (
                            <button 
                              onClick={() => handleUndo(section.id, section.llmResult!)}
                              className="text-[10px] font-bold text-primary hover:underline uppercase transition-all"
                            >
                              ⟲ Vrati na prethodnu verziju
                            </button>
                          )}
                        </div>
                        <TextArea
                          name={`refined-${section.id}`}
                          value={section.refinedResult || ''}
                          onChange={(e) => {
                            const val = e.target.value;
                            setDocument(prev => ({
                              ...prev!,
                              sections: prev!.sections.map(s => s.id === section.id ? {...s, refinedResult: val} : s)
                            }))
                          }}
                          onBlur={(e) => handleRefinedChange(section.id, e.target.value)}
                          className="flex-1 min-h-[250px] border-primary/30 bg-surface-2/60 font-serif text-base leading-relaxed text-ink shadow-inner focus:bg-surface-2 focus:border-primary/50 transition-all outline-none custom-scrollbar"
                        />
                      </>
                    ) : (
                      <div className="flex h-full min-h-[250px] flex-col items-center justify-center rounded-lg border-2 border-dashed border-hairline bg-surface-2/20 p-8 text-center text-ink-subtle italic">
                        <svg className="mb-3 h-8 w-8 opacity-20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                        </svg>
                        Unesite podatke lijevo i kliknite "Generiši" da započnete AI pisanje.
                      </div>
                    )}
                  </div>
                </div>
                
                {hasAiResult && (
                  <div className="mt-8 border-t border-hairline pt-6">
                    <div className="flex flex-wrap items-center justify-between gap-4 rounded-lg bg-surface-2/30 p-4 border border-hairline/50">
                      <div className="space-y-1">
                        <h4 className="text-[10px] font-bold uppercase tracking-wider text-ink-subtle">Kvalitet AI rezultata:</h4>
                        <div className="flex gap-1.5">
                          {[1, 2, 3, 4, 5].map(num => (
                            <button 
                              key={num}
                              onClick={() => handleSaveFeedback(section.id, num, section.feedbackComment || '')}
                              className={`h-9 w-9 rounded-md border text-sm font-bold transition-all ${
                                section.rating === num 
                                  ? 'bg-primary border-primary text-white scale-110 shadow-lg shadow-primary/20' 
                                  : 'border-hairline bg-surface-3/50 text-ink-subtle hover:text-ink hover:border-primary/50'
                              }`}
                            >
                              {num}
                            </button>
                          ))}
                        </div>
                      </div>
                      <div className="flex-1 min-w-[200px]">
                        <input 
                          type="text" 
                          placeholder="Dodajte komentar menadžeru (obavezno za finalizaciju)..." 
                          className="w-full rounded-md border border-hairline bg-surface-3/50 px-4 py-2.5 text-sm text-ink outline-none focus:border-primary/60 focus:bg-surface-3 transition-all placeholder:text-ink-tertiary"
                          defaultValue={section.feedbackComment}
                          onBlur={(e) => handleSaveFeedback(section.id, section.rating || 0, e.target.value)}
                        />
                      </div>
                    </div>
                  </div>
                )}
              </section>
            )
          })}
        </div>
      </div>
    </AppShell>
  )
}