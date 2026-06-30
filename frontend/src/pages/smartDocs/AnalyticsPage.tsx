import { useState } from 'react'
import { AppShell } from '../../components/layout/AppShell'
import { Button } from '../../components/ui/Button'
import { fetchAiAnalytics } from '../../api/smartDocs'
import type { AiAnalyticsReport } from '../../api/smartDocs'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts'

export function AnalyticsPage() {
  const [data, setData] = useState<AiAnalyticsReport[]>([])
  const [loading, setLoading] = useState(false)
  const [dates, setDates] = useState({ 
    start: new Date(new Date().setMonth(new Date().getMonth() - 1)).toISOString().split('T')[0],
    end: new Date().toISOString().split('T')[0] 
  })

  const loadReport = async () => {
    setLoading(true)
    try {
      const report = await fetchAiAnalytics(
        new Date(dates.start).toISOString(), 
        new Date(dates.end).toISOString()
      )
      setData(report)
    } catch {
      alert("Greška pri dobavljanju podataka.")
    } finally { setLoading(false) }
  }

  return (
    <AppShell>
      <div className="max-w-6xl mx-auto p-6 space-y-8 print-container">
        
        <header className="flex justify-between items-end border-b border-hairline pb-6 no-print">
          <div>
            <h1 className="text-3xl font-bold text-ink">AI analitički izvještaj</h1>
            <p className="text-ink-subtle">Upravljanje kvalitetom LLM asistencije</p>
          </div>
          <div className="flex gap-4 items-end bg-surface-2 p-4 rounded-xl border border-hairline">
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-bold text-ink-muted uppercase">Od:</span>
              <input type="date" className="bg-canvas border border-hairline p-2 rounded text-sm" value={dates.start} onChange={e => setDates({...dates, start: e.target.value})} />
            </div>
            <div className="flex flex-col gap-1">
              <span className="text-[10px] font-bold text-ink-muted uppercase">Do:</span>
              <input type="date" className="bg-canvas border border-hairline p-2 rounded text-sm" value={dates.end} onChange={e => setDates({...dates, end: e.target.value})} />
            </div>
            <Button onClick={loadReport} disabled={loading}>{loading ? 'Obrađujem...' : 'Generiši'}</Button>
          </div>
        </header>

        {data.length > 0 ? (
          <div id="report-content" className="space-y-8 bg-transparent">
            
            <div className="only-print">
              <div className="flex justify-between items-start border-b-2 border-black pb-4 mb-10">
                <div>
                  <h1 className="text-2xl font-bold text-black uppercase tracking-tight">Izvještaj o efikasnosti AI asistenta</h1>
                  <p className="text-black font-medium">Istraživačko-Razvojni Institut</p>
                </div>
                <div className="text-right text-xs text-black">
                  <p><strong>Period:</strong> {dates.start} - {dates.end}</p>
                  <p><strong>Generisano:</strong> {new Date().toLocaleDateString('sr-RS')}</p>
                </div>
              </div>
            </div>

            {/* GRAFIKON */}
            <div className="bg-surface-1 p-8 rounded-2xl border border-hairline shadow-sm pdf-section">
              <h3 className="text-sm font-bold mb-8 text-primary uppercase tracking-widest only-screen">Grafički prikaz performansi</h3>
              <h3 className="text-sm font-bold mb-8 text-black uppercase tracking-widest only-print">1. Vizuelizacija rejtinga i valjanosti</h3>
              <div className="h-80 w-full">
                <ResponsiveContainer>
                  <BarChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} strokeOpacity={0.1} />
                    <XAxis dataKey="templateName" tick={{fontSize: 10, fill: '#000'}} axisLine={{stroke: '#000'}} />
                    <YAxis tick={{fontSize: 10, fill: '#000'}} axisLine={{stroke: '#000'}} />
                    <Tooltip contentStyle={{color: '#000'}} />
                    <Legend verticalAlign="top" height={36}/>
                    <Bar dataKey="averageRating" fill="#3b82f6" name="Prosječna Ocjena" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="avgHumanEditRatio" fill="#10b981" name="Index Valjanosti" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="overflow-hidden rounded-2xl border border-hairline bg-surface-1 pdf-section">
              <table className="w-full text-left border-collapse pdf-table">
                <thead className="bg-surface-2 border-b border-hairline text-[11px] font-black uppercase text-ink-muted">
                  <tr>
                    <th className="px-8 py-5">Šablon dokumenta</th>
                    <th className="px-8 py-5 text-center">Broj sekcija</th>
                    <th className="px-8 py-5 text-center">Ocjena</th>
                    <th className="px-8 py-5 text-center">Index valjanosti</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-hairline">
                  {data.map(row => (
                    <tr key={row.templateId}>
                      <td className="px-8 py-5 font-bold text-ink">{row.templateName}</td>
                      <td className="px-8 py-5 text-center text-ink-subtle">{row.totalGeneratedSections}</td>
                      <td className="px-8 py-5 text-center font-bold">⭐ {row.averageRating}</td>
                      <td className="px-8 py-5 text-center font-bold text-success">{(row.avgHumanEditRatio * 100).toFixed(0)}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex justify-end no-print pt-4">
               <Button variant="secondary" onClick={() => window.print()}>
                 Preuzmi PDF
               </Button>
            </div>
          </div>
        ) : null}
      </div>

      <style>{`
        .only-print { display: none; }
        .only-screen { display: block; }

        @media print { 
          * { 
            background-color: white !important; 
            color: black !important; 
            box-shadow: none !important; 
            text-shadow: none !important;
            border-color: #ddd !important;
          }

          .no-print, aside, nav, .sidebar, header, button { display: none !important; } 
          .only-print { display: block !important; }
          .only-screen { display: none !important; }

          body { background: white !important; padding: 0; margin: 0; }
          
          #report-content { 
            margin: 0 !important; 
            padding: 0 !important; 
            width: 100% !important;
          }

          .pdf-table { 
            width: 100% !important; 
            border: 1px solid #000 !important;
            background-color: white !important;
          }
          .pdf-table thead { background-color: #f2f2f2 !important; }
          .pdf-table th, .pdf-table td { 
            border: 1px solid #eee !important; 
            color: black !important; 
            background-color: white !important;
          }
          
          .recharts-text { fill: black !important; font-weight: bold; }
          .recharts-cartesian-axis-line, .recharts-cartesian-axis-tick-line { stroke: black !important; }

          /* Osigurava da se boje barova vide */
          .recharts-rectangle { -webkit-print-color-adjust: exact !important; }
        }
      `}</style>
    </AppShell>
  )
}