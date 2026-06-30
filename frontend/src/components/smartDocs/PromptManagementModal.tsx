import { useEffect, useState } from 'react'
import { Modal } from '../ui/Modal'
import { TextArea } from '../ui/TextArea'
import { Button } from '../ui/Button'
import { fetchPromptHistory, createNewPromptVersion, activatePromptVersion, type PromptVersion } from '../../api/smartDocs'
import { formatDate } from '../../lib/formatDate'

interface Props {
  isOpen: boolean
  onClose: () => void
  sectionId: number
  sectionTitle: string
}

export function PromptManagementModal({ isOpen, onClose, sectionId, sectionTitle }: Props) {
  const [history, setHistory] = useState<PromptVersion[]>([])
  const [newContent, setNewContent] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let isMounted = true; 

    async function loadData() {
      if (!isOpen) return;
      
      try {
        const data = await fetchPromptHistory(sectionId);
        if (isMounted) {
          setHistory(data);
          const active = data.find(v => v.active);
          if (active) setNewContent(active.content);
        }
      } catch (err) {
        console.error("Greška pri učitavanju:", err);
      }
    }

    void loadData();

    return () => { isMounted = false; }; 
  }, [isOpen, sectionId]); 

  const handleSave = async () => {
    if (!newContent.trim()) return
  setLoading(true)
  try {
    await createNewPromptVersion(sectionId, newContent)
    
    const updatedData = await fetchPromptHistory(sectionId)
    setHistory(updatedData)

    const newActive = updatedData.find(v => v.active)
    if (newActive) {
      setNewContent(newActive.content)
    }

    alert("Nova verzija prompta je sačuvana!")
  } catch {
    alert("Greška pri čuvanju.")
  } finally {
    setLoading(false)
  }
  }

  const handleActivate = async (versionId: number) => {
    if (!window.confirm("Da li želite da vratite ovu verziju kao aktivnu?")) return
  try {
    await activatePromptVersion(sectionId, versionId)
    
    const updatedData = await fetchPromptHistory(sectionId)
    setHistory(updatedData)

    const newActive = updatedData.find(v => v.active)
    if (newActive) {
      setNewContent(newActive.content)
    }
    
  } catch {
    alert("Greška pri aktivaciji.")
  }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Upravljanje Promptom: ${sectionTitle}`} className="max-w-3xl">
      <div className="space-y-8">
        <section className="space-y-4">
          <TextArea 
            label="Izmeni sistemski prompt" 
            name={`prompt-editor-${sectionId}`} 
            value={newContent} 
            onChange={(e) => setNewContent(e.target.value)}
            className="min-h-[200px] font-mono text-xs text-ink"
          />
          <div className="flex justify-end">
            <Button onClick={handleSave} disabled={loading}>
              {loading ? 'Čuvanje...' : 'Sačuvaj kao novu verziju'}
            </Button>
          </div>
        </section>

        <section className="space-y-4">
          <h4 className="text-xs font-bold uppercase tracking-widest text-ink-subtle">Istorija verzija</h4>
          <div className="rounded-lg border border-hairline overflow-hidden bg-surface-2">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-3 border-b border-hairline text-ink-muted">
                <tr>
                  <th className="px-4 py-2">v.</th>
                  <th className="px-4 py-2">Tekst (isječak)</th>
                  <th className="px-4 py-2">Ocjena (prosjek)</th>
                  <th className="px-4 py-2">Datum</th>
                  <th className="px-4 py-2 text-right">Akcija</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-hairline">
                {history.map(v => (
                  <tr key={v.id} className={v.active ? "bg-primary/5" : "hover:bg-surface-3/50"}>
                    <td className="px-4 py-3 font-bold text-ink">{v.versionNumber}</td>
                    <td className="px-4 py-3 text-ink-subtle truncate max-w-[200px]">{v.content}</td>
                     <td className="px-4 py-3">
                      {v.feedbackCount && v.feedbackCount > 0 ? (
                        <div className="flex flex-col gap-1">
                            <span className="font-bold text-ink">
                            ⭐ {v.averageRating?.toFixed(1)} <span className="text-[10px] text-ink-subtle font-normal">({v.feedbackCount})</span>
                            </span>
                       <button 
                         onClick={() => alert(`Komentari za v${v.versionNumber}:\n\n` + v.feedbackComments?.join('\n\n'))}
                         className="text-[10px] text-primary hover:underline font-semibold text-left">
                          Vidi komentare
                       </button>
                        </div>
                       ) : (
                          <span className="text-[10px] text-ink-tertiary italic">Nema feedback-a</span>
                          )}
                    </td>

                    <td className="px-4 py-3 text-ink-subtle">{formatDate(v.createdAt)}</td>
                    <td className="px-4 py-3 text-right">
                      {v.active ? (
                        <span className="text-primary font-bold">AKTIVAN</span>
                      ) : (
                        <button 
                          onClick={() => handleActivate(v.id)}
                          className="text-primary hover:underline cursor-pointer font-medium"
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
    </Modal>
  )
}