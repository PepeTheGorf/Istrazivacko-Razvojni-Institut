import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { apiFetch } from '../../api/client' 
import type { SmartTemplate } from '../../types/smartDocs'
import { formatDate } from '../../lib/formatDate'

export function TemplateListPage() {
  const [templates, setTemplates] = useState<SmartTemplate[]>([])
  const [loading, setLoading] = useState(true)

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
            <h1 className="text-3xl font-semibold text-ink">Moji Šabloni</h1>
            <p className="text-ink-subtle">Pregled i upravljanje pametnim šablonima za generisanje.</p>
          </div>
          <Link to="/smart-templates/new">
            <Button>+ Novi Šablon</Button>
          </Link>
        </header>

        <div className="rounded-xl border border-hairline bg-surface-1 overflow-hidden">
          {loading ? (
            <p className="p-8 text-center text-ink-subtle">Učitavanje šablona...</p>
          ) : templates.length > 0 ? (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-surface-2 border-b border-hairline">
                  <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-ink-muted">Naziv</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-ink-muted">Oblast / Tip</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-ink-muted">Datum</th>
                  <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-ink-muted text-right">Sekcije</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-hairline">
                {templates.map(t => (
                  <tr key={t.id} className="hover:bg-surface-2/50 transition-colors">
                    <td className="px-6 py-4 text-sm font-medium text-ink">{t.domain?.name} / {t.category?.name}</td>
                    <td className="px-6 py-4 text-sm text-ink-subtle">
                      {t.domain.name} / {t.category.name}
                    </td>
                    <td className="px-6 py-4 text-sm text-ink-muted">{formatDate(t.createdAt)}</td>
                    <td className="px-6 py-4 text-sm text-right font-mono text-primary">{t.sections?.length || 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="p-12 text-center">
              <p className="text-ink-subtle">Još uvek niste kreirali nijedan šablon.</p>
              <Link to="/smart-templates/new" className="mt-4 inline-block text-primary hover:underline text-sm">
                Napravite svoj prvi šablon sada
              </Link>
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}