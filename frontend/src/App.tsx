import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { GuestRoute } from './components/auth/GuestRoute'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ProjectDetailsPage } from './pages/projects/ProjectDetailsPage'
import { ProjectFormPage } from './pages/projects/ProjectFormPage'
import { ProjectsPage } from './pages/projects/ProjectsPage'
import { TaskDetailsPage } from './pages/projects/TaskDetailsPage'
import { MyTaskDetailsPage } from './pages/teamMemberTasks/MyTaskDetailsPage'
import { MyTasksPage } from './pages/teamMemberTasks/MyTasksPage'
import { WorkflowCreateWizardPage } from './pages/workflowCreate/WorkflowCreateWizardPage'
import { WorkflowEditWizardPage } from './pages/workflows/WorkflowEditWizardPage'
import { WorkflowsPage } from './pages/workflows/WorkflowsPage'
import { TemplateCreateWizardPage } from './pages/smartTemplates/TemplateCreateWizardPage'
import { TemplateSelectionPage } from './pages/smartDocs/TemplateSelectionPage'
import { DocumentEditorPage } from './pages/smartDocs/DocumentEditorPage'

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<GuestRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['MANAGER']} />}>
          <Route path="/projects" element={<ProjectsPage />} />
          <Route path="/projects/:projectId" element={<ProjectDetailsPage />} />
          <Route path="/projects/:projectId/tasks/:taskId" element={<TaskDetailsPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['TEAM_MEMBER']} />}>
          <Route path="/my-tasks" element={<MyTasksPage />} />
          <Route path="/my-tasks/tasks/:taskId" element={<MyTaskDetailsPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['MANAGER']} />}>
          <Route path="/projects/new" element={<ProjectFormPage />} />
          <Route path="/projects/:projectId/edit" element={<ProjectFormPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['ADMINISTRATOR']} />}>
          <Route path="/workflows" element={<WorkflowsPage />} />
          <Route path="/workflows/new" element={<WorkflowCreateWizardPage />} />
          <Route path="/workflows/:workflowId/edit" element={<WorkflowEditWizardPage />} />
        </Route>

        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />

        <Route element={<ProtectedRoute allowedRoles={['MANAGER']} />}>
            <Route path="/smart-templates" element={<div>Lista šablona (U izradi)</div>} />
            <Route path="/smart-templates/new" element={<TemplateCreateWizardPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['TEAM_MEMBER']} />}>
          <Route path="/smart-docs" element={<TemplateSelectionPage />} />
          <Route path="/smart-docs/:docId" element={<DocumentEditorPage />} />
        </Route>
        
      </Routes>
    </AuthProvider>
  )
}

export default App
