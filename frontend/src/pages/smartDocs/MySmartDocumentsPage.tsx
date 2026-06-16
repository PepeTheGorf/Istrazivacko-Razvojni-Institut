import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { ProgressBar } from '../../components/ui/ProgressBar' // Nova komponenta
import { Breadcrumbs } from '../../components/ui/Breadcrumbs' // Nova komponenta
import { fetchMySmartDocuments } from '../../api/smartDocs'
import type { SmartDocumentSummary } from '../../types/smartDocs'
import { formatDate } from '../../lib/formatDate'

export function MySmartDocumentsPage() {
  const navigate = useNavigate()
  const [docs, setDocs] = useState<SmartDocumentSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<'ALL' | 'DRAFT' | 'COMPLETED'>('ALL')

  useEffect(() => {
    fetchMySmartDocuments()
      .then(setDocs)
      .finally(() => setLoading(false))
  }, [])

  // ISPRAVKA: Filtriramo niz za prikaz
  const filteredDocs = docs.filter(doc => {
    if (filter === 'ALL') return true
    return doc.status === filter
  })

  return (
    <AppShell>
      <div className="mx-auto max-w-6xl">
        {/* PUTOKAZI */}
        <Breadcrumbs items={[
          { label: 'AI Dokumentacija', to: '/my-smart-docs' },
          { label: 'Moja Lista' }
        ]} />

        <header className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-semibold text-ink">Moja AI Dokumentacija</h1>
            <p className="text-ink-subtle">Pregled i upravljanje vašim generisanim radovima.</p>
          </div>
          <Button onClick={() => navigate('/smart-docs')}>+ Kreiraj novi</Button>
        </header>

        {/* FILTERI */}
        <div className="mb-6 flex gap-2">
          {(['ALL', 'DRAFT', 'COMPLETED'] as const).map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-1.5 text-xs font-semibold rounded-full border transition-all ${
                filter === f 
                  ? 'bg-primary border-primary text-on-primary' 
                  : 'border-hairline text-ink-subtle hover:border-ink-subtle'
              }`}
            >
              {f === 'ALL' ? 'Sve' : f === 'DRAFT' ? 'U radu' : 'Završeno'}
            </button>
          ))}
        </div>

        <div className="rounded-xl border border-hairline bg-surface-1 overflow-hidden">
          {loading ? (
            <p className="p-8 text-center text-ink-subtle">Učitavanje...</p>
          ) : filteredDocs.length > 0 ? (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-surface-2 border-b border-hairline">
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted">Šablon</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted">Datum kreiranja</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted">Progres</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted">Status</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted text-right">Akcija</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-hairline">
                {filteredDocs.map(doc => (
                  <tr key={doc.id} className="hover:bg-surface-2/50 transition-colors">
                    <td className="px-6 py-4 text-sm font-medium text-ink">{doc.templateName}</td>
                    <td className="px-6 py-4 text-sm text-ink-subtle">{formatDate(doc.createdAt)}</td>
                    {/* PROGRES BAR KOLONA */}
                    <td className="px-6 py-4 w-48">
                      <ProgressBar progress={doc.progress || 0} />
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <span className={`rounded-full px-2 py-1 text-xs font-medium ${
                        doc.status === 'COMPLETED' 
                          ? 'bg-success/10 text-success' 
                          : 'bg-primary/10 text-primary'
                      }`}>
                        {doc.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      {doc.status === 'COMPLETED' ? (
                        <Button variant="secondary" onClick={() => navigate(`/smart-docs/${doc.id}/view`)}>
                          Pregledaj
                        </Button>
                      ) : (
                        <Button variant="primary" onClick={() => navigate(`/smart-docs/${doc.id}`)}>
                          Otvori Editor
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="p-12 text-center text-ink-subtle">
              Nema dokumenata u ovoj kategoriji.
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}