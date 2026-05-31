export const MOCK_PROJECT_NAME = 'Razvoj Fullstack Turističke Aplikacije'

export const MOCK_TASK_ID = 'mock-demo'

export type MockTaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE'

export type MockTaskCard = {
  id: string
  title: string
  dueDate: string
  projectName: string
  status: MockTaskStatus
}

export type MockPhaseColumn = {
  id: MockTaskStatus
  title: string
  defaultExpanded: boolean
  tasks: MockTaskCard[]
}

export const MOCK_PHASE_COLUMNS: MockPhaseColumn[] = [
  {
    id: 'TODO',
    title: 'To Do',
    defaultExpanded: true,
    tasks: [
      {
        id: 'mock-todo-1',
        title: 'Razvijanje Backenda',
        dueDate: '16/04/2026',
        projectName: MOCK_PROJECT_NAME,
        status: 'TODO',
      },
      {
        id: 'mock-todo-2',
        title: 'Razvijanje Backenda',
        dueDate: '16/04/2026',
        projectName: MOCK_PROJECT_NAME,
        status: 'TODO',
      },
      {
        id: 'mock-todo-3',
        title: 'Razvijanje Backenda',
        dueDate: '16/04/2026',
        projectName: MOCK_PROJECT_NAME,
        status: 'TODO',
      },
    ],
  },
  {
    id: 'IN_PROGRESS',
    title: 'In Progress',
    defaultExpanded: true,
    tasks: [
      {
        id: MOCK_TASK_ID,
        title: 'Implementacija Backenda',
        dueDate: '16/04/2026',
        projectName: MOCK_PROJECT_NAME,
        status: 'IN_PROGRESS',
      },
      {
        id: 'mock-progress-2',
        title: 'Razvijanje Backenda',
        dueDate: '16/04/2026',
        projectName: MOCK_PROJECT_NAME,
        status: 'IN_PROGRESS',
      },
    ],
  },
  {
    id: 'IN_REVIEW',
    title: 'In Review',
    defaultExpanded: false,
    tasks: [],
  },
  {
    id: 'DONE',
    title: 'Done',
    defaultExpanded: false,
    tasks: [],
  },
]

export type SubtaskStatus = 'In Progress' | 'Done' | 'In Review'

export type MockSubtask = {
  id: string
  title: string
  status: SubtaskStatus
  children?: MockSubtask[]
}

export type MockCriterion = {
  id: string
  label: string
  checked: boolean
}

export const MOCK_TASK_DETAILS = {
  id: MOCK_TASK_ID,
  title: 'Implementacija Backenda',
  phase: 'In Progress' as SubtaskStatus,
  assignees: 'Petar Petrović, Nikola Nikolić',
  dueDate: '30/4/2026',
  workflow: 'Razvoj Softvera',
  spentTime: '5 dana, 12h',
  criteria: [
    { id: 'c1', label: 'Kreirati model baze podataka u kodu', checked: true },
    { id: 'c2', label: 'Implementirati REST API', checked: true },
    { id: 'c3', label: 'Povezati API sa bazom podataka', checked: true },
    { id: 'c4', label: 'Napisati unit testove', checked: false },
    { id: 'c5', label: 'Dokumentovati API', checked: false },
    { id: 'c6', label: 'Konfigurisati CI/CD pipeline', checked: false },
    { id: 'c7', label: 'Pripremiti seed podatke', checked: true },
  ] satisfies MockCriterion[],
  subtasks: [
    {
      id: 's1',
      title: 'Dizajn API endpoint-a',
      status: 'In Progress',
      children: [
        {
          id: 's1-1',
          title: 'POST /customers endpoint',
          status: 'Done',
          children: [
            { id: 's1-1-1', title: 'Validacija unosa od strane korisnika', status: 'Done' },
            { id: 's1-1-2', title: 'Mapiranje DTO i entiteta', status: 'Done' },
          ],
        },
        {
          id: 's1-2',
          title: 'GET /customers endpoint',
          status: 'In Review',
          children: [{ id: 's1-2-1', title: 'Paginacija i filteri', status: 'In Review' }],
        },
        { id: 's1-3', title: 'PUT /customers/{id} endpoint', status: 'In Progress' },
      ],
    },
    {
      id: 's2',
      title: 'Implementacija servisnog sloja',
      status: 'In Progress',
      children: [
        {
          id: 's2-1',
          title: 'CustomerService implementacija',
          status: 'In Progress',
          children: [
            { id: 's2-1-1', title: 'CRUD operacije', status: 'Done' },
            { id: 's2-1-2', title: 'Validacija poslovnih pravila', status: 'In Progress' },
            { id: 's2-1-2-1', title: 'Provera jedinstvenog email-a', status: 'In Progress' },
          ],
        },
        {
          id: 's2-2',
          title: 'ReservationService implementacija',
          status: 'In Review',
          children: [
            { id: 's2-2-1', title: 'Rezervacija termina', status: 'In Review' },
            { id: 's2-2-2', title: 'Otkazivanje rezervacije', status: 'Done' },
          ],
        },
      ],
    },
    {
      id: 's3',
      title: 'Integracija sa bazom podataka',
      status: 'Done',
      children: [
        { id: 's3-1', title: 'Migracije i šema', status: 'Done' },
        {
          id: 's3-2',
          title: 'Repozitorijumi i upiti',
          status: 'Done',
          children: [
            { id: 's3-2-1', title: 'JPA repozitorijumi', status: 'Done' },
            { id: 's3-2-2', title: 'Optimizacija upita', status: 'Done' },
            { id: 's3-2-2-1', title: 'Indeksi za česte pretrage', status: 'Done' },
          ],
        },
      ],
    },
    {
      id: 's4',
      title: 'Testiranje i QA',
      status: 'In Review',
      children: [
        { id: 's4-1', title: 'Unit testovi za servise', status: 'In Review' },
        {
          id: 's4-2',
          title: 'Integracioni testovi API-ja',
          status: 'In Progress',
          children: [
            { id: 's4-2-1', title: 'Testovi autentifikacije', status: 'In Progress' },
            { id: 's4-2-2', title: 'Testovi CRUD endpoint-a', status: 'In Review' },
            { id: 's4-2-2-1', title: 'POST /customers scenariji', status: 'Done' },
            { id: 's4-2-2-2', title: 'GET /customers scenariji', status: 'In Review' },
          ],
        },
      ],
    },
  ] satisfies MockSubtask[],
}
