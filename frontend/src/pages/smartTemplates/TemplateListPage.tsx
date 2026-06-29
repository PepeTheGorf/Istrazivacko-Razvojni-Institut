import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom' 
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { apiFetch } from '../../api/client' 
import type { SmartTemplate } from '../../types/smartDocs'
import { formatDate } from '../../lib/formatDate'

export function TemplateListPage() {
  const [templates, setTemplates] = useState<SmartTemplate[]>([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate() 

  useEffect(() => {
    apiFetch<SmartTemplate[]>('/smart-docs/templates/all')
      .then(setTemplates)
      .finally(() => setLoading(false))
  }, [])

  return (
    <AppShell>
      <div className="mx-auto max-w-6xl">
        <header className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-semibold text-ink">Moji šabloni</h1>
            <p className="text-ink-subtle">Analiza kvaliteta i upravljanje šablonima.</p>
          </div>
          <Link to="/smart-templates/new">
            <Button>+ Novi šablon</Button>
          </Link>
        </header>

        <div className="rounded-xl border border-hairline bg-surface-1 overflow-hidden">
          {loading ? (
            <p className="p-8 text-center text-ink-subtle">Učitavanje šablona...</p>
          ) : templates.length > 0 ? (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-surface-2 border-b border-hairline">
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted">Naziv / Oblast</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted">AI Rejting</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted">Datum</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted text-right">Sekcije</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase text-ink-muted text-right">Akcije</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-hairline text-sm">
                {templates.map(t => (
                  <tr 
                    key={t.id} 
                    className="group hover:bg-surface-2/80 transition-all cursor-pointer"
                    onClick={() => navigate(`/smart-templates/${t.id}`)}
                  >
                    <td className="px-6 py-4">
                      <div className="font-medium text-ink group-hover:text-primary transition-colors">{t.name}</div>
                      <div className="text-xs text-ink-subtle">{t.domain?.name} / {t.category?.name}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <span className="text-sm font-bold text-primary">
                          {t.averageRating ? t.averageRating.toFixed(1) : '0.0'}
                        </span>
                        <div className="h-1.5 w-24 rounded-full bg-surface-3 overflow-hidden">
                          <div 
                            className="h-full bg-yellow-500 transition-all duration-1000" 
                            style={{ width: `${(t.averageRating || 0) * 20}%` }} 
                          />
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-ink-muted">
                      {formatDate(t.createdAt)} 
                    </td>
                    <td className="px-6 py-4 text-right font-mono text-primary">
                      {t.sections?.length || 0}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <span className="text-xs font-medium text-primary opacity-0 group-hover:opacity-100 transition-opacity">
                        Upravljaj →
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="p-12 text-center text-ink-subtle">Nema kreiranih šablona.</div>
          )}
        </div>
      </div>
    </AppShell>
  )
}