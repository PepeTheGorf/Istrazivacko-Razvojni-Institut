import type { TooltipProps } from 'recharts'
import { CHART_COLORS, chartTooltipStyle } from '../lib/chartTheme'

export function AnalyticsChartTooltip({
  active,
  payload,
  label,
}: TooltipProps<number, string>) {
  if (!active || !payload?.length) {
    return null
  }

  return (
    <div
      style={{
        ...chartTooltipStyle,
        padding: '10px 12px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.45)',
      }}
    >
      {label && (
        <p
          style={{
            margin: 0,
            marginBottom: 6,
            color: CHART_COLORS.ink,
            fontSize: 12,
            fontWeight: 600,
          }}
        >
          {label}
        </p>
      )}
      <ul style={{ margin: 0, padding: 0, listStyle: 'none' }}>
        {payload.map((entry) => {
          const displayValue =
            (entry.payload as { avgHoursLabel?: string } | undefined)?.avgHoursLabel ??
            entry.value

          return (
            <li
              key={`${entry.name}-${entry.dataKey}`}
              style={{
                margin: 0,
                color: CHART_COLORS.muted,
                fontSize: 12,
                lineHeight: 1.5,
              }}
            >
              <span style={{ color: entry.color ?? CHART_COLORS.primary }}>{entry.name}</span>
              {': '}
              <span style={{ color: CHART_COLORS.ink }}>{displayValue}</span>
            </li>
          )
        })}
      </ul>
    </div>
  )
}
