import type { ProjectTask } from '../../../types/task'

export const PREVIEW_MOCK_TASK_ID_PREFIX = 'preview-mock-'

export function isPreviewMockTask(task: ProjectTask): boolean {
  return Boolean(task.id?.startsWith(PREVIEW_MOCK_TASK_ID_PREFIX))
}

function createMockSubSubTask(
  id: string,
  name: string,
  description: string,
  phaseName: string,
  endDate: string,
): ProjectTask {
  return {
    id: `${PREVIEW_MOCK_TASK_ID_PREFIX}${id}`,
    name,
    description,
    phaseName,
    endDate,
  }
}

function createMockSubTask(
  id: string,
  name: string,
  description: string,
  phaseName: string,
  endDate: string,
  subSubTasks?: ProjectTask[],
): ProjectTask {
  return {
    id: `${PREVIEW_MOCK_TASK_ID_PREFIX}${id}`,
    name,
    description,
    phaseName,
    endDate,
    subTasks: subSubTasks,
  }
}

/** Attaches fictional subtasks for UI preview when project already has real tasks. */
export function withPreviewSubTasks(tasks: ProjectTask[]): ProjectTask[] {
  if (tasks.length === 0) {
    return tasks
  }

  return tasks.map((task, index) => {
    if ((task.subTasks?.length ?? 0) > 0) {
      return task
    }

    const variant = index % 3

    if (variant === 0) {
      return {
        ...task,
        subTasks: [
          createMockSubTask(
            `sub-${index}-a`,
            'Pod-zadatak: API integracija',
            'Povezivanje frontend forme sa backend endpoint-ima za zadatke.',
            'U toku',
            '2026-06-18T12:00:00Z',
            [
              createMockSubSubTask(
                `sub-${index}-a-1`,
                'Pod-pod-zadatak: DTO mapiranje',
                'Uskladiti request/response modele između FE i BE.',
                'Planirano',
                '2026-06-10T12:00:00Z',
              ),
              createMockSubSubTask(
                `sub-${index}-a-2`,
                'Pod-pod-zadatak: Validacija payload-a',
                'Provera obaveznih polja i formata datuma.',
                'U toku',
                '2026-06-12T12:00:00Z',
              ),
            ],
          ),
          createMockSubTask(
            `sub-${index}-b`,
            'Pod-zadatak: UI review',
            'Provera thread prikaza i modala za kreiranje pod-zadataka.',
            'Na čekanju',
            '2026-06-22T12:00:00Z',
          ),
        ],
      }
    }

    if (variant === 1) {
      return {
        ...task,
        subTasks: [
          createMockSubTask(
            `sub-${index}-a`,
            'Pod-zadatak: Testni scenariji',
            'Definisati osnovne E2E i unit testove za task flow.',
            'U toku',
            '2026-06-14T12:00:00Z',
            [
              createMockSubSubTask(
                `sub-${index}-a-1`,
                'Pod-pod-zadatak: Happy path',
                'Kreiranje, izmena i brisanje zadatka.',
                'Završeno',
                '2026-06-08T12:00:00Z',
              ),
            ],
          ),
        ],
      }
    }

    return {
      ...task,
      subTasks: [
        createMockSubTask(
          `sub-${index}-a`,
          'Pod-zadatak: Dokumentacija',
          'Ažurirati README i API primer poziva za menadžera.',
          'Planirano',
          '2026-06-30T12:00:00Z',
        ),
        createMockSubTask(
          `sub-${index}-b`,
          'Pod-zadatak: Resursi tima',
          'Dodela članova i tehničkih resursa za realizaciju.',
          'U toku',
          '2026-06-20T12:00:00Z',
          [
            createMockSubSubTask(
              `sub-${index}-b-1`,
              'Pod-pod-zadatak: Tehnički resursi',
              'Provera dostupnosti opreme i alata.',
              'Na čekanju',
              '2026-06-16T12:00:00Z',
            ),
          ],
        ),
      ],
    }
  })
}
