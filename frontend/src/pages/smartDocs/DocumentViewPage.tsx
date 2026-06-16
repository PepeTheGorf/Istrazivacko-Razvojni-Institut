// src/pages/smartDocs/DocumentViewPage.tsx
import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { fetchDocumentById } from '../../api/smartDocs'
import type { SmartDocument } from '../../types/smartDocs'

export function DocumentViewPage() {
  const { docId } = useParams<{ docId: string }>()
  const navigate = useNavigate()
  const [document, setDocument] = useState<SmartDocument | null>(null)

  useEffect(() => {
    if (docId) fetchDocumentById(docId).then(setDocument)
  }, [docId])

  if (!document) return <AppShell>Učitavanje...</AppShell>

  return (
    <AppShell>
      <div className="mx-auto max-w-3xl bg-white p-12 shadow-sm rounded-lg text-black min-h-[29.7cm]">
        <header className="mb-12 border-b-2 border-primary pb-6 flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold uppercase tracking-tight">{document.templateName}</h1>
            <p className="text-gray-500 mt-2">ID Dokumenta: {document.id} | Status: {document.status}</p>
          </div>
          <Button variant="secondary" onClick={() => navigate('/my-smart-docs')}>Zatvori</Button>
        </header>

        <div className="space-y-10">
          {document.sections.map((section) => (
            <section key={section.id}>
              <h2 className="text-xl font-semibold mb-4 border-b border-gray-200 pb-2">{section.title}</h2>
              <div className="text-base leading-relaxed text-gray-800 whitespace-pre-wrap italic">
                {section.llmResult || "Ova sekcija nije generisana."}
              </div>
            </section>
          ))}
        </div>
        
        <footer className="mt-20 pt-8 border-t border-gray-200 text-sm text-gray-400 text-center">
          Generisano putem AI sistema - Istraživačko Razvojni Institut © {new Date().getFullYear()}
        </footer>
      </div>
    </AppShell>
  )
}