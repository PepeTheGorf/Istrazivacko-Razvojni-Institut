import jsPDF from 'jspdf'
import dejaVuSansBoldUrl from 'dejavu-fonts-ttf/ttf/DejaVuSans-Bold.ttf?url'
import dejaVuSansUrl from 'dejavu-fonts-ttf/ttf/DejaVuSans.ttf?url'

const FONT_REGULAR_FILE = 'DejaVuSans-Regular.ttf'
const FONT_BOLD_FILE = 'DejaVuSans-Bold.ttf'
export const PDF_FONT_FAMILY = 'DejaVuSans'

let cachedRegularBase64: string | null = null
let cachedBoldBase64: string | null = null

function arrayBufferToBase64(buffer: ArrayBuffer) {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.length; i += 1) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}

async function loadFontBase64(url: string, cache: 'regular' | 'bold') {
  if (cache === 'regular' && cachedRegularBase64) return cachedRegularBase64
  if (cache === 'bold' && cachedBoldBase64) return cachedBoldBase64

  const response = await fetch(url)
  if (!response.ok) {
    throw new Error('Unicode PDF font could not be loaded')
  }

  const base64 = arrayBufferToBase64(await response.arrayBuffer())
  if (cache === 'regular') cachedRegularBase64 = base64
  else cachedBoldBase64 = base64

  return base64
}

function registerUnicodeFonts(doc: jsPDF, regularBase64: string, boldBase64: string) {
  doc.addFileToVFS(FONT_REGULAR_FILE, regularBase64)
  doc.addFileToVFS(FONT_BOLD_FILE, boldBase64)
  doc.addFont(FONT_REGULAR_FILE, PDF_FONT_FAMILY, 'normal')
  doc.addFont(FONT_BOLD_FILE, PDF_FONT_FAMILY, 'bold')
}

export function applyPdfFont(doc: jsPDF, style: 'normal' | 'bold' = 'normal') {
  doc.setFont(PDF_FONT_FAMILY, style)
}

export async function createUnicodePdfDocument() {
  const [regularBase64, boldBase64] = await Promise.all([
    loadFontBase64(dejaVuSansUrl, 'regular'),
    loadFontBase64(dejaVuSansBoldUrl, 'bold'),
  ])

  const doc = new jsPDF()
  registerUnicodeFonts(doc, regularBase64, boldBase64)
  applyPdfFont(doc, 'normal')
  return doc
}

export function addInstituteHeader(doc: jsPDF) {
  const pageWidth = doc.internal.pageSize.getWidth()
  const margin = 14

  applyPdfFont(doc, 'bold')
  doc.setFontSize(11)
  doc.text('Istrazivacko Razvojni Institut', pageWidth - margin, 16, { align: 'right' })
  applyPdfFont(doc, 'normal')
}

export function formatPdfDateTime(date: Date) {
  const day = date.getDate()
  const month = date.getMonth() + 1
  const year = date.getFullYear()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${day}.${month}.${year}. ${hours}:${minutes}:${seconds}`
}

export const pdfTableStyles = {
  font: PDF_FONT_FAMILY,
  fontStyle: 'normal' as const,
  fontSize: 10,
}

export const pdfTableHeadStyles = {
  font: PDF_FONT_FAMILY,
  fontStyle: 'normal' as const,
  fillColor: [94, 106, 210] as [number, number, number],
  textColor: [255, 255, 255] as [number, number, number],
}

export const pdfAutoTableDefaults = {
  styles: pdfTableStyles,
  headStyles: pdfTableHeadStyles,
  bodyStyles: pdfTableStyles,
  didParseCell: (data: { cell: { styles: { font?: string; fontStyle?: string } }; section: string }) => {
    data.cell.styles.font = PDF_FONT_FAMILY
    data.cell.styles.fontStyle = 'normal'
  },
}
