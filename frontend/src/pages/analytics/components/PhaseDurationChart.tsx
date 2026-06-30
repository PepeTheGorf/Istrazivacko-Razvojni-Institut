import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { PhaseAnalytics } from '../../../types/analytics'
import { formatDurationHours } from '../../../lib/formatDuration'
import { CHART_COLORS, chartAxisTick } from '../lib/chartTheme'
import { AnalyticsChartTooltip } from './AnalyticsChartTooltip'

interface PhaseDurationChartProps {
  phases: PhaseAnalytics[]
  duplicatePhaseNames: Set<string>
}

function phaseLabel(phase: PhaseAnalytics, duplicatePhaseNames: Set<string>) {
  return duplicatePhaseNames.has(phase.phaseName)
    ? `${phase.phaseName} (#${phase.phaseOrder})`
    : phase.phaseName
}

export function PhaseDurationChart({ phases, duplicatePhaseNames }: PhaseDurationChartProps) {
  const durationData = phases.map((phase) => ({
    name: phaseLabel(phase, duplicatePhaseNames),
    avgHours: Number((phase.averageSecondsInPhase / 3600).toFixed(2)),
    avgHoursLabel: formatDurationHours(phase.averageSecondsInPhase),
    taskCount: phase.currentTaskCount,
  }))

  if (durationData.length === 0) {
    return null
  }

  const tooltipCursor = { fill: 'rgba(94, 106, 210, 0.12)' }

  return (
    <div className="grid gap-8">
      <div className="h-72 min-w-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={durationData} margin={{ top: 8, right: 8, left: 0, bottom: 48 }}>
            <CartesianGrid stroke={CHART_COLORS.grid} strokeDasharray="3 3" vertical={false} />
            <XAxis
              dataKey="name"
              tick={chartAxisTick}
              axisLine={{ stroke: CHART_COLORS.grid }}
              tickLine={false}
              interval={0}
              angle={0}
              textAnchor="end"
              height={64}
            />
            <YAxis
              tick={chartAxisTick}
              axisLine={false}
              tickLine={false}
              label={{
                value: 'Prosečno vreme (h)',
                angle: -90,
                position: 'insideLeft',
                fill: CHART_COLORS.muted,
                fontSize: 12,
              }}
            />
            <Tooltip
              content={<AnalyticsChartTooltip />}
              cursor={tooltipCursor}
              formatter={(value, _name, item) => [
                item.payload?.avgHoursLabel ?? `${value} h`,
                'Prosečno vreme u fazi',
              ]}
            />
            <Bar dataKey="avgHours" name="Prosečno vreme u fazi" fill={CHART_COLORS.primary} radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="h-72 min-w-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={durationData} margin={{ top: 8, right: 8, left: 0, bottom: 48 }}>
            <CartesianGrid stroke={CHART_COLORS.grid} strokeDasharray="3 3" vertical={false} />
            <XAxis
              dataKey="name"
              tick={chartAxisTick}
              axisLine={{ stroke: CHART_COLORS.grid }}
              tickLine={false}
              interval={0}
              angle={0}
              textAnchor="end"
              height={64}
            />
            <YAxis
              allowDecimals={false}
              tick={chartAxisTick}
              axisLine={false}
              tickLine={false}
              label={{
                value: 'Broj zadataka',
                angle: -90,
                position: 'insideLeft',
                fill: CHART_COLORS.muted,
                fontSize: 12,
              }}
            />
            <Tooltip content={<AnalyticsChartTooltip />} cursor={tooltipCursor} />
            <Legend wrapperStyle={{ color: CHART_COLORS.muted, fontSize: 12 }} />
            <Bar dataKey="taskCount" name="Zadaci u fazi" fill={CHART_COLORS.success} radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
