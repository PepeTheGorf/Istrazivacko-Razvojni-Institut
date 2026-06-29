package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.UserDTO;
import org.example.projectrealizationservice.dto.analytics.PhaseAnalyticsDTO;
import org.example.projectrealizationservice.dto.analytics.ProjectWorkflowAnalysisDTO;
import org.example.projectrealizationservice.dto.analytics.TaskTeamMemberStatsDTO;
import org.example.projectrealizationservice.feign.UserServiceClient;
import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.repository.ProjectRepository;
import org.example.projectrealizationservice.repository.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.TaskPhaseTransitionsRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.service.ProjectAnalyticsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectAnalyticsServiceImpl implements ProjectAnalyticsService {

    private final TaskPhaseTransitionsRepository taskPhaseTransitionsRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    private final UserServiceClient userServiceClient;

    @Override
    public ProjectWorkflowAnalysisDTO analyzeProjectWorkflow(Long projectId) {
        Project project = requireProject(projectId);

        List<PhaseAnalyticsDTO> phaseAnalyticsDTOs = taskRepository.aggregatePhaseTaskCountsByProject(project).stream()
                .map(aggregate -> {
                    String phaseName = aggregate.getPhaseName();
                    int phaseOrder = aggregate.getPhaseOrder();
                    Double averageSeconds = taskPhaseTransitionsRepository.averageDurationByPhaseNameAndOrder(
                            project,
                            phaseName,
                            phaseOrder
                    );
                    return PhaseAnalyticsDTO.builder()
                            .phaseId((long) Objects.hash(phaseName, phaseOrder))
                            .phaseName(phaseName)
                            .phaseOrder(phaseOrder)
                            .currentTaskCount(aggregate.getTaskCount().intValue())
                            .averageSecondsInPhase(averageSeconds == null ? 0.0 : averageSeconds)
                            .build();
                })
                .toList();

        return ProjectWorkflowAnalysisDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .phaseAnalytics(phaseAnalyticsDTOs)
                .totalTasks(taskRepository.countTasksByProject(project))
                .completedTasks(taskRepository.countCompletedTasksByProject(project))
                .activeTasks(taskRepository.countTasksByProject(project) - taskRepository.countCompletedTasksByProject(project))
                .overdueTasks(taskRepository.countOverdueTasksByProject(project))
                .build();
    }

    @Override
    public List<TaskTeamMemberStatsDTO> analyzeTaskTeamMemberStats(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));

        Project project = task.getProject();
        if (project == null || project.getId() == null) {
            throw new RuntimeException("Task is not associated with a project!");
        }

        requireProject(project.getId());

        return taskAssignmentRepository.findByTask_Id(taskId).stream()
                .map(taskAssignment -> buildTeamMemberStats(taskAssignment.getAssigneeId(), project.getId()))
                .toList();
    }

    @Override
    public List<TaskTeamMemberStatsDTO> analyzeProjectTeamMemberStats(Long projectId) {
        requireProject(projectId);

        return taskAssignmentRepository.findDistinctAssigneeIdsByProjectId(projectId).stream()
                .map(assigneeId -> buildTeamMemberStats(assigneeId, projectId))
                .toList();
    }

    private TaskTeamMemberStatsDTO buildTeamMemberStats(Long assigneeId, Long projectId) {
        UserDTO user = userServiceClient.getUserById(assigneeId);

        int totalAssignedTasks = taskAssignmentRepository.countTaskAssignmentsByAssigneeIdAndProject(assigneeId, projectId);
        int completedTasks = taskAssignmentRepository.countCompletedTasksByAssigneeAndProject(assigneeId, projectId);
        int overdueTasks = taskAssignmentRepository.countOverdueTasksByAssigneeAndProject(assigneeId, projectId);

        return TaskTeamMemberStatsDTO.builder()
                .memberId(assigneeId)
                .memberName(user.getFirstName() + " " + user.getLastName())
                .totalAssignedTasks(totalAssignedTasks)
                .completedTasks(completedTasks)
                .activeTasks(totalAssignedTasks - completedTasks)
                .overdueTasks(overdueTasks)
                .build();
    }

    private Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
    }
}
