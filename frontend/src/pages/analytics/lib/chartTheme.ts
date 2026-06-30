export const CHART_COLORS = {
  primary: '#5e6ad2',
  success: '#27a644',
  error: '#e5484d',
  muted: '#8a8f98',
  grid: '#23252a',
  surface: '#141516',
  border: '#34343a',
  ink: '#f7f8f8',
} as const

export const chartTooltipStyle = {
  backgroundColor: CHART_COLORS.surface,
  border: `1px solid ${CHART_COLORS.border}`,
  borderRadius: 8,
  color: CHART_COLORS.ink,
  fontSize: 12,
  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.45)',
} as const

export const chartAxisTick = { fill: CHART_COLORS.muted, fontSize: 12 }
