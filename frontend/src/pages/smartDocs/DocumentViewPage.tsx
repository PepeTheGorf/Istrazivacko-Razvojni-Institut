import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { Breadcrumbs } from '../../components/ui/Breadcrumbs'
import { fetchDocumentById } from '../../api/smartDocs'
import type { SmartDocument } from '../../types/smartDocs'
import { formatDate } from '../../lib/formatDate'

export function DocumentViewPage() {
  const { docId } = useParams<{ docId: string }>()
  const navigate = useNavigate()
  const [document, setDocument] = useState<SmartDocument | null>(null)

  useEffect(() => {
    if (docId) fetchDocumentById(docId).then(setDocument).catch(console.error)
  }, [docId])

  if (!document) return <AppShell>Učitavanje...</AppShell>

  return (
    <AppShell>
      <div className="mx-auto max-w-4xl">
        <div className="print:hidden">
          <Breadcrumbs items={[
            { label: 'Moja Dokumentacija', to: '/my-smart-docs' },
            { label: 'Pregled Dokumenta' }
          ]} />
        </div>

        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
          {/* HEADER */}
          <header className="p-8 border-b border-gray-100 bg-gray-50 flex justify-between items-start">
            <div>
              <h1 className="text-3xl font-bold text-black uppercase tracking-tight">
                {document.templateName}
              </h1>
              <div className="mt-2 text-sm text-gray-500">
                <p>Datum izdavanja: {formatDate(document.createdAt)}</p>
                <p>Status: <span className="text-green-600 font-bold">{document.status}</span></p>
              </div>
            </div>
            
            <div className="flex gap-3 print:hidden">
              <Button variant="tertiary" onClick={() => window.print()}>
                Preuzmi PDF / Štampaj
              </Button>
              <Button variant="secondary" onClick={() => navigate('/my-smart-docs')}>
                Zatvori
              </Button>
            </div>
          </header>

          <div className="p-12 space-y-12 bg-white text-black min-h-[20cm]">
            {document.sections.map((section) => (
              <section key={section.id} className="prose max-w-none">
                <h2 className="text-xl font-bold border-b border-gray-200 pb-2 mb-4 text-black">
                  {section.title}
                </h2>
                <div className="text-lg leading-relaxed whitespace-pre-wrap text-gray-800 italic">
                  {section.llmResult || "Sadržaj za ovu sekciju nije generisan."}
                </div>
              </section>
            ))}
          </div>

          <footer className="p-8 bg-gray-50 border-t border-gray-100 text-center text-xs text-gray-400">
            Zvanični dokument Instituta • Generisano pomoću AI sistema • {new Date().getFullYear()}
          </footer>
        </div>
      </div>
    </AppShell>
  )
}