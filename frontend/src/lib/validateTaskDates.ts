export interface TaskDateConstraints {
  projectStartDate?: string
  projectEndDate?: string
  parentStartDate?: string
  parentEndDate?: string
}

function parseDate(value?: string): Date | null {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

export function validateTaskDates(
  startDate: string,
  endDate: string,
  constraints: TaskDateConstraints,
): string | null {
  const start = parseDate(startDate)
  const end = parseDate(endDate)

  if (start && end && start > end) {
    return 'Datum početka zadatka ne može biti posle datuma završetka.'
  }

  const projectStart = parseDate(constraints.projectStartDate)
  const projectEnd = parseDate(constraints.projectEndDate)

  if (projectStart && start && start < projectStart) {
    return 'Zadatak ne može početi pre početka projekta.'
  }
  if (projectEnd && end && end > projectEnd) {
    return 'Zadatak ne može završiti nakon kraja projekta.'
  }
  if (projectEnd && start && start > projectEnd) {
    return 'Zadatak ne može početi nakon kraja projekta.'
  }

  const parentStart = parseDate(constraints.parentStartDate)
  const parentEnd = parseDate(constraints.parentEndDate)

  if (parentStart && start && start < parentStart) {
    return 'Podzadatak mora početi nakon početka parent zadatka.'
  }
  if (parentEnd && end && end > parentEnd) {
    return 'Podzadatak mora završiti pre kraja parent zadatka.'
  }

  return null
}
