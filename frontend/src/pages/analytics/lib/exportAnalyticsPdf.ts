import html2canvas from 'html2canvas'
import autoTable from 'jspdf-autotable'
import { formatDurationHours } from '../../../lib/formatDuration'
import type {
  AnalyticsFilters,
  PhaseAnalytics,
  ProjectWorkflowAnalysis,
  TaskTeamMemberStats,
} from '../../../types/analytics'
import {
  addInstituteHeader,
  applyPdfFont,
  createUnicodePdfDocument,
  formatPdfDateTime,
  pdfAutoTableDefaults,
} from './pdfFonts'

type JsPdfWithAutoTable = Awaited<ReturnType<typeof createUnicodePdfDocument>> & {
  lastAutoTable?: { finalY: number }
}

const PDF_MARGIN = 14

function sanitizeFileName(value: string) {
  return (
    value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^\w\s-]/g, '')
      .trim()
      .replace(/\s+/g, '-')
      .toLowerCase() || 'report'
  )
}

function formatFilterSummary(filters?: Partial<AnalyticsFilters>) {
  const parts: string[] = []
  if (filters?.from) parts.push(`Od: ${filters.from}`)
  if (filters?.to) parts.push(`Do: ${filters.to}`)
  if (filters?.memberId) parts.push(`Član: ${filters.memberId}`)
  if (filters?.taskId) parts.push(`Zadatak: ${filters.taskId}`)
  return parts.length > 0 ? parts.join(' | ') : 'Bez aktivnih filtera'
}

function phaseLabel(phase: PhaseAnalytics, duplicatePhaseNames: Set<string>) {
  return duplicatePhaseNames.has(phase.phaseName)
    ? `${phase.phaseName} (#${phase.phaseOrder})`
    : phase.phaseName
}

function beginPdfPage(doc: JsPdfWithAutoTable, y = 26) {
  addInstituteHeader(doc)
  applyPdfFont(doc, 'normal')
  return y
}

function writeReportMeta(
  doc: JsPdfWithAutoTable,
  y: number,
  projectName: string,
  filters?: Partial<AnalyticsFilters>,
) {
  doc.setFontSize(10)
  doc.text(`Projekat: ${projectName}`, PDF_MARGIN, y)
  y += 6
  doc.text(`Generisano: ${formatPdfDateTime(new Date())}`, PDF_MARGIN, y)
  y += 6
  doc.text(formatFilterSummary(filters), PDF_MARGIN, y)
  return y + 8
}

async function addChartImage(
  doc: JsPdfWithAutoTable,
  element: HTMLElement | null,
  startY: number,
) {
  if (!element) return startY

  const canvas = await html2canvas(element, {
    backgroundColor: '#0f1011',
    scale: 2,
    logging: false,
  })

  const pageWidth = doc.internal.pageSize.getWidth()
  const pageHeight = doc.internal.pageSize.getHeight()
  const imgWidth = pageWidth - PDF_MARGIN * 2
  const imgHeight = (canvas.height * imgWidth) / canvas.width
  let y = startY

  if (y + imgHeight > pageHeight - PDF_MARGIN) {
    doc.addPage()
    y = beginPdfPage(doc)
  }

  doc.addImage(canvas.toDataURL('image/png'), 'PNG', PDF_MARGIN, y, imgWidth, imgHeight)
  return y + imgHeight + 10
}

function getTableEndY(doc: JsPdfWithAutoTable, fallback: number) {
  return doc.lastAutoTable?.finalY ?? fallback
}

export async function exportWorkflowAnalyticsPdf({
  workflow,
  phases,
  duplicatePhaseNames,
  filters,
  chartElement,
}: {
  workflow: ProjectWorkflowAnalysis
  phases: PhaseAnalytics[]
  duplicatePhaseNames: Set<string>
  filters?: Partial<AnalyticsFilters>
  chartElement: HTMLElement | null
}) {
  const doc = (await createUnicodePdfDocument()) as JsPdfWithAutoTable
  let y = beginPdfPage(doc)

  doc.setFontSize(16)
  doc.text('Statistika po fazama radnog toka', PDF_MARGIN, y)
  y += 8
  y = writeReportMeta(doc, y, workflow.projectName, filters)

  autoTable(doc, {
    ...pdfAutoTableDefaults,
    startY: y,
    head: [['Metrika', 'Vrednost']],
    body: [
      ['Ukupno zadataka', String(workflow.totalTasks)],
      ['Završeni', String(workflow.completedTasks)],
      ['Aktivni', String(workflow.activeTasks)],
      ['Prekoračeni rok', String(workflow.overdueTasks)],
      ['Ukupno trajanje', formatDurationHours(workflow.totalTaskDurationSeconds)],
      ['Prosečno trajanje zadatka', formatDurationHours(workflow.averageTaskDurationSeconds)],
    ],
  })

  y = getTableEndY(doc, y) + 10
  y = await addChartImage(doc, chartElement, y)

  if (phases.length > 0) {
    if (y > doc.internal.pageSize.getHeight() - 40) {
      doc.addPage()
      y = beginPdfPage(doc)
    }

    autoTable(doc, {
      ...pdfAutoTableDefaults,
      startY: y,
      head: [['Redosled', 'Faza', 'Broj zadataka', 'Prosečno vreme u fazi']],
      body: phases.map((phase) => [
        String(phase.phaseOrder),
        phaseLabel(phase, duplicatePhaseNames),
        String(phase.currentTaskCount),
        formatDurationHours(phase.averageSecondsInPhase),
      ]),
    })
  }

  doc.save(`workflow-analytics-${sanitizeFileName(workflow.projectName)}.pdf`)
}

export async function exportTeamMemberAnalyticsPdf({
  projectName,
  teamStats,
  filters,
  chartElement,
}: {
  projectName: string
  teamStats: TaskTeamMemberStats[]
  filters?: Partial<AnalyticsFilters>
  chartElement: HTMLElement | null
}) {
  const doc = (await createUnicodePdfDocument()) as JsPdfWithAutoTable
  let y = beginPdfPage(doc)

  const totals = {
    assigned: teamStats.reduce((sum, member) => sum + member.totalAssignedTasks, 0),
    completed: teamStats.reduce((sum, member) => sum + member.completedTasks, 0),
    active: teamStats.reduce((sum, member) => sum + member.activeTasks, 0),
    overdue: teamStats.reduce((sum, member) => sum + member.overdueTasks, 0),
  }

  doc.setFontSize(16)
  doc.text('Statistika o članovima tima', PDF_MARGIN, y)
  y += 8
  y = writeReportMeta(doc, y, projectName, filters)

  autoTable(doc, {
    ...pdfAutoTableDefaults,
    startY: y,
    head: [['Metrika', 'Vrednost']],
    body: [
      ['Dodeljeno', String(totals.assigned)],
      ['Završeno', String(totals.completed)],
      ['Aktivno', String(totals.active)],
      ['Prekoračeno', String(totals.overdue)],
    ],
  })

  y = getTableEndY(doc, y) + 10
  y = await addChartImage(doc, chartElement, y)

  if (teamStats.length > 0) {
    if (y > doc.internal.pageSize.getHeight() - 40) {
      doc.addPage()
      y = beginPdfPage(doc)
    }

    autoTable(doc, {
      ...pdfAutoTableDefaults,
      startY: y,
      styles: { ...pdfAutoTableDefaults.styles, fontSize: 9 },
      head: [['Član', 'Dodeljeno', 'Završeno', 'Aktivno', 'Prekoračeno', 'Prosečno trajanje']],
      body: teamStats.map((member) => [
        member.memberName,
        String(member.totalAssignedTasks),
        String(member.completedTasks),
        String(member.activeTasks),
        String(member.overdueTasks),
        formatDurationHours(member.averageTaskDurationSeconds),
      ]),
    })
  }

  doc.save(`team-analytics-${sanitizeFileName(projectName)}.pdf`)
}
