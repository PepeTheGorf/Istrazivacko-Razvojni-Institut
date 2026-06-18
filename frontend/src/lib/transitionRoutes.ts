import type { TransitionConditionCreation } from '../types/workflow'

export function routeKey(from: string, to: string): string {
  return `${from}→${to}`
}

export function findRoute(
  conditions: TransitionConditionCreation[],
  from: string,
  to: string,
): TransitionConditionCreation | undefined {
  return conditions.find((c) => c.from === from && c.to === to)
}

export function routeExists(
  conditions: TransitionConditionCreation[],
  from: string,
  to: string,
  except?: { from: string; to: string },
): boolean {
  if (except && except.from === from && except.to === to) return false
  return conditions.some((c) => c.from === from && c.to === to)
}
