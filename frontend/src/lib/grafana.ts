const GRAFANA_BASE = import.meta.env.VITE_GRAFANA_BASE_URL ?? 'http://localhost:3000'

export const GRAFANA_DASHBOARD_UID = 'manager-analytics'

export function getGrafanaBaseUrl() {
  return GRAFANA_BASE.replace(/\/$/, '')
}

export function buildGrafanaDashboardUrl(
  projectId: string,
  statistic: 'workflow' | 'team',
) {
  const panelId = statistic === 'workflow' ? '1' : '2'
  const params = new URLSearchParams({
    orgId: '1',
    'var-project_id': projectId,
    'var-statistic': statistic,
    viewPanel: panelId,
  })

  return `${getGrafanaBaseUrl()}/d/${GRAFANA_DASHBOARD_UID}/menadzer-analitika?${params.toString()}`
}
