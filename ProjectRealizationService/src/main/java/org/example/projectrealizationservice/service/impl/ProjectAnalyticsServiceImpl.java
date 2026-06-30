package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.UserDTO;
import org.example.projectrealizationservice.dto.analytics.AnalyticsFilter;
import org.example.projectrealizationservice.dto.analytics.PhaseAnalyticsDTO;
import org.example.projectrealizationservice.dto.analytics.ProjectWorkflowAnalysisDTO;
import org.example.projectrealizationservice.dto.analytics.TaskPhaseHistoryEntryDTO;
import org.example.projectrealizationservice.dto.analytics.TaskTeamMemberStatsDTO;
import org.example.projectrealizationservice.feign.UserServiceClient;
import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.repository.ProjectRepository;
import org.example.projectrealizationservice.repository.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.TaskDurationAggregate;
import org.example.projectrealizationservice.repository.TaskPhaseHistoryAggregate;
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
    public ProjectWorkflowAnalysisDTO analyzeProjectWorkflow(Long projectId, AnalyticsFilter filter) {
        Project project = requireProject(projectId);
        AnalyticsFilter effectiveFilter = filter != null ? filter : AnalyticsFilter.empty();

        Long memberId = effectiveFilter.memberId();
        Long taskId = effectiveFilter.taskId();
        var from = effectiveFilter.from();
        var to = effectiveFilter.to();

        List<PhaseAnalyticsDTO> phaseAnalyticsDTOs = taskRepository
                .aggregatePhaseTaskCountsByProject(project, memberId, taskId, from, to)
                .stream()
                .map(aggregate -> {
                    String phaseName = aggregate.getPhaseName();
                    int phaseOrder = aggregate.getPhaseOrder();
                    Double averageSeconds = taskPhaseTransitionsRepository.averageDurationByPhaseNameAndOrder(
                            project,
                            phaseName,
                            phaseOrder,
                            memberId,
                            taskId,
                            from,
                            to
                    );
                    return PhaseAnalyticsDTO.builder()
                            .phaseId(aggregate.getPhaseId())
                            .phaseName(phaseName)
                            .phaseOrder(phaseOrder)
                            .currentTaskCount(aggregate.getTaskCount().intValue())
                            .averageSecondsInPhase(averageSeconds == null ? 0.0 : averageSeconds)
                            .build();
                })
                .toList();

        int totalTasks = taskRepository.countTasksByProject(project, memberId, taskId, from, to);
        int completedTasks = taskRepository.countCompletedTasksByProject(project, memberId, taskId, from, to);
        DurationSummary durationSummary = computeDurationSummary(project, memberId, taskId, from, to);

        List<TaskPhaseHistoryEntryDTO> taskPhaseHistory = List.of();
        if (taskId != null) {
            taskPhaseHistory = taskPhaseTransitionsRepository.findTaskPhaseHistory(taskId, from, to).stream()
                    .map(entry -> TaskPhaseHistoryEntryDTO.builder()
                            .fromPhaseName(entry.getFromPhaseName())
                            .toPhaseName(entry.getToPhaseName())
                            .durationSeconds(entry.getDurationSeconds())
                            .transitionedAt(entry.getTransitionedAt())
                            .build())
                    .toList();
        }

        return ProjectWorkflowAnalysisDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .phaseAnalytics(phaseAnalyticsDTOs)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .activeTasks(totalTasks - completedTasks)
                .overdueTasks(taskRepository.countOverdueTasksByProject(project, memberId, taskId, from, to))
                .totalTaskDurationSeconds(durationSummary.totalSeconds())
                .averageTaskDurationSeconds(durationSummary.averageSeconds())
                .taskPhaseHistory(taskPhaseHistory)
                .build();
    }

    @Override
    public List<TaskTeamMemberStatsDTO> analyzeTaskTeamMemberStats(Long taskId, AnalyticsFilter filter) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));

        Project project = task.getProject();
        if (project == null || project.getId() == null) {
            throw new RuntimeException("Task is not associated with a project!");
        }

        requireProject(project.getId());

        AnalyticsFilter scopedFilter = new AnalyticsFilter(
                filter != null ? filter.from() : null,
                filter != null ? filter.to() : null,
                filter != null ? filter.memberId() : null,
                taskId
        );

        return taskAssignmentRepository.findByTask_Id(taskId).stream()
                .map(taskAssignment -> buildTeamMemberStats(taskAssignment.getAssigneeId(), project.getId(), scopedFilter))
                .filter(member -> scopedFilter.memberId() == null
                        || Objects.equals(member.getMemberId(), scopedFilter.memberId()))
                .toList();
    }

    @Override
    public List<TaskTeamMemberStatsDTO> analyzeProjectTeamMemberStats(Long projectId, AnalyticsFilter filter) {
        requireProject(projectId);
        AnalyticsFilter effectiveFilter = filter != null ? filter : AnalyticsFilter.empty();

        return taskAssignmentRepository.findDistinctAssigneeIdsByProjectId(projectId).stream()
                .filter(assigneeId -> effectiveFilter.memberId() == null
                        || Objects.equals(assigneeId, effectiveFilter.memberId()))
                .map(assigneeId -> buildTeamMemberStats(assigneeId, projectId, effectiveFilter))
                .toList();
    }

    private TaskTeamMemberStatsDTO buildTeamMemberStats(Long assigneeId, Long projectId, AnalyticsFilter filter) {
        UserDTO user = userServiceClient.getUserById(assigneeId);

        Long taskId = filter.taskId();
        var from = filter.from();
        var to = filter.to();

        int totalAssignedTasks = taskAssignmentRepository.countTaskAssignmentsByAssigneeIdAndProject(
                assigneeId, projectId, taskId, from, to);
        int completedTasks = taskAssignmentRepository.countCompletedTasksByAssigneeAndProject(
                assigneeId, projectId, taskId, from, to);
        int overdueTasks = taskAssignmentRepository.countOverdueTasksByAssigneeAndProject(
                assigneeId, projectId, taskId, from, to);

        DurationSummary memberDuration = computeDurationSummary(
                requireProject(projectId),
                assigneeId,
                taskId,
                from,
                to
        );

        return TaskTeamMemberStatsDTO.builder()
                .memberId(assigneeId)
                .memberName(user.getFirstName() + " " + user.getLastName())
                .totalAssignedTasks(totalAssignedTasks)
                .completedTasks(completedTasks)
                .activeTasks(totalAssignedTasks - completedTasks)
                .overdueTasks(overdueTasks)
                .averageTaskDurationSeconds(memberDuration.averageSeconds())
                .build();
    }

    private DurationSummary computeDurationSummary(
            Project project,
            Long memberId,
            Long taskId,
            java.time.OffsetDateTime from,
            java.time.OffsetDateTime to
    ) {
        List<TaskDurationAggregate> perTaskDurations = taskPhaseTransitionsRepository.sumDurationGroupedByTask(
                project, memberId, taskId, from, to);

        if (perTaskDurations.isEmpty()) {
            return new DurationSummary(0.0, 0.0);
        }

        double totalSeconds = perTaskDurations.stream()
                .mapToDouble(aggregate -> aggregate.getTotalDurationSeconds())
                .sum();
        double averageSeconds = totalSeconds / perTaskDurations.size();

        return new DurationSummary(totalSeconds, averageSeconds);
    }

    private Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
    }

    private record DurationSummary(double totalSeconds, double averageSeconds) {}
}
