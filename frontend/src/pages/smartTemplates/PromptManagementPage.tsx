import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { AppShell } from '../../components/layout/AppShell'
import { TextArea } from '../../components/ui/TextArea'
import { Button } from '../../components/ui/Button'
import { Breadcrumbs } from '../../components/ui/Breadcrumbs'
import { 
  fetchPromptHistory, 
  createNewPromptVersion, 
  activatePromptVersion, 
  fetchTemplateById,
  type PromptVersion 
} from '../../api/smartDocs'
import type { SmartTemplate } from '../../types/smartDocs'
import { formatDate } from '../../lib/formatDate'

export function PromptManagementPage() {
  const { templateId, sectionId } = useParams()
  
  const [template, setTemplate] = useState<SmartTemplate | null>(null)
  const [history, setHistory] = useState<PromptVersion[]>([])
  const [newContent, setNewContent] = useState('')
  const [loading, setLoading] = useState(false)
  const [selectedVersionForComments, setSelectedVersionForComments] = useState<PromptVersion | null>(null)

  useEffect(() => {
    async function loadInitialData() {
      if (!templateId || !sectionId) return
      
      try {
        const templateData = await fetchTemplateById(Number(templateId))
        setTemplate(templateData)

        const historyData = await fetchPromptHistory(Number(sectionId))
        setHistory(historyData)

        const active = historyData.find(v => v.active)
        if (active) {
          setNewContent(active.content)
          setSelectedVersionForComments(active)
        }
      } catch (err) {
        console.error("Greška pri učitavanju:", err)
      }
    }
    loadInitialData()
  }, [templateId, sectionId])

  const handleSave = async () => {
    if (!newContent.trim() || !sectionId) return
    setLoading(true)
    try {
      await createNewPromptVersion(Number(sectionId), newContent)
      const updated = await fetchPromptHistory(Number(sectionId))
      setHistory(updated)
      alert("Uspešno sačuvana nova verzija!")
    } catch {
      alert("Greška pri čuvanju.")
    } finally {
      setLoading(false)
    }
  }

  const handleActivate = async (vId: number) => {
    if (!window.confirm("Aktivirati ovu verziju?")) return
    try {
      await activatePromptVersion(Number(sectionId), vId)
      const updated = await fetchPromptHistory(Number(sectionId))
      setHistory(updated)
    } catch {
      alert("Greška pri aktivaciji.")
    }
  }

  const sectionTitle = template?.sections.find(s => s.id === Number(sectionId))?.title || "Sekcija"

  return (
    <AppShell>
      <div className="flex flex-col h-[calc(100vh-120px)]">
        <Breadcrumbs items={[
          { label: 'Šabloni', to: '/smart-templates' },
          { label: template?.name || 'Šablon', to: `/smart-templates/${templateId}` },
          { label: sectionTitle }
        ]} />

        <div className="mt-4 flex flex-1 gap-6 overflow-hidden">
          <div className="flex-1 overflow-y-auto space-y-6 pr-2 custom-scrollbar">
            <header>
                <h1 className="text-2xl font-bold text-ink">Upravljanje promptom</h1>
                <p className="text-ink-subtle">Sekcija: {sectionTitle}</p>
            </header>

            <section className="bg-surface-1 border border-hairline rounded-xl p-5 shadow-sm space-y-4">
              <TextArea 
                label="Napiši novu verziju prompta"
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                className="min-h-[300px] font-mono text-sm"
              />
              <div className="flex justify-end">
                <Button onClick={handleSave} disabled={loading}>
                  {loading ? 'Čuvanje...' : 'Sačuvaj kao novu verziju'}
                </Button>
              </div>
            </section>

            <section className="space-y-3">
              <h3 className="text-sm font-bold uppercase tracking-wider text-ink-muted px-1">Prethodne verzije</h3>
              <div className="bg-surface-1 border border-hairline rounded-xl overflow-hidden shadow-sm">
                <table className="w-full text-left text-sm">
                  <thead className="bg-surface-2 border-b border-hairline text-ink-muted text-xs uppercase">
                    <tr>
                      <th className="px-4 py-3">Ver.</th>
                      <th className="px-4 py-3">Ocena</th>
                      <th className="px-4 py-3">Datum</th>
                      <th className="px-4 py-3 text-right">Akcija</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-hairline">
                    {history.map(v => (
                      <tr 
                        key={v.id} 
                        onClick={() => setSelectedVersionForComments(v)}
                        className={`cursor-pointer transition-colors ${selectedVersionForComments?.id === v.id ? 'bg-primary/5' : 'hover:bg-surface-2/50'}`}
                      >
                        <td className="px-4 py-4 font-medium">
                            <span className={v.active ? "text-primary" : "text-ink"}>v{v.versionNumber}</span>
                            {v.active && <span className="ml-2 text-[10px] bg-primary/10 text-primary px-1.5 py-0.5 rounded">Aktivna</span>}
                        </td>
                        <td className="px-4 py-4">
                           {v.feedbackCount ? `⭐ ${v.averageRating?.toFixed(1)} (${v.feedbackCount})` : <span className="text-ink-tertiary">-</span>}
                        </td>
                        <td className="px-4 py-4 text-ink-subtle text-xs">{formatDate(v.createdAt)}</td>
                        <td className="px-4 py-4 text-right">
                          {!v.active && (
                            <button 
                              onClick={(e) => { e.stopPropagation(); handleActivate(v.id); }}
                              className="text-primary font-semibold hover:underline"
                            >
                              Aktiviraj
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          </div>

          {/* DESNI PANEL: Komentari */}
          <aside className="w-96 bg-surface-1 border border-hairline rounded-xl flex flex-col shadow-sm overflow-hidden">
            <div className="p-4 border-b border-hairline bg-surface-2/50">
              <h3 className="font-bold text-ink flex items-center gap-2">
                💬 Komentari korisnika
                {selectedVersionForComments && (
                  <span className="text-xs font-normal text-ink-subtle">za v{selectedVersionForComments.versionNumber}</span>
                )}
              </h3>
            </div>
            
            <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-surface-2/20">
              {!selectedVersionForComments ? (
                <div className="text-center py-20 text-ink-tertiary text-sm italic">
                  Izaberite verziju iz tabele levo da vidite komentare
                </div>
              ) : selectedVersionForComments.feedbackComments && selectedVersionForComments.feedbackComments.length > 0 ? (
                selectedVersionForComments.feedbackComments.map((c, i) => (
                  <div key={i} className="p-4 bg-surface-3 border border-hairline rounded-lg shadow-sm text-sm text-ink-subtle leading-relaxed italic">
                    {c}
                  </div>
                ))
              ) : (
                <div className="text-center py-20 text-ink-tertiary text-sm italic">
                  Ova verzija još uvek nema komentara.
                </div>
              )}
            </div>
          </aside>
        </div>
      </div>
    </AppShell>
  )
}