package org.example.projectrealizationservice.mapper;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.*;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TaskAssignment;
import org.example.projectrealizationservice.model.TaskResourceAssignment;
import org.example.projectrealizationservice.repository.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.repository.TaskResourceAssignmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskViewMapper {

    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final TaskRepository taskRepository;

    public ProjectTaskDTO toProjectTaskDto(Task task) {
        Long taskId = task.getId();
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTask_Id(taskId);
        List<TaskResourceAssignment> resourceAssignments = taskResourceAssignmentRepository.findByTask_Id(taskId);

        List<TaskResourceAssignmentDTO> technicalResources = resourceAssignments.stream()
                .map(assignment -> TaskResourceAssignmentDTO.fromAssignment(
                        assignment, assignment.getTechnicalResource()))
                .toList();

        List<Long> assigneeIds = assignments.stream()
                .map(TaskAssignment::getAssigneeId)
                .toList();
        Long assigneeId = assigneeIds.isEmpty() ? null : assigneeIds.getFirst();

        List<ProjectTaskDTO> subTasks = taskRepository.findSubtasksByParentTaskId(taskId).stream()
                .map(this::toProjectTaskDto)
                .toList();

        return ProjectTaskDTO.builder()
                .id(taskId)
                .name(task.getName())
                .description(task.getDescription())
                .phaseName(task.getPhase() != null ? task.getPhase().getName() : null)
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .creatorId(task.getCreatorId())
                .assigneeId(assigneeId)
                .assigneeIds(assigneeIds)
                .workflow(WorkflowDTO.toDTO(task.getWorkflow()))
                .technicalResources(technicalResources)
                .acceptanceCriteria(acceptanceCriteriaRepository.findByTask_Id(taskId).stream()
                        .map(AcceptanceCriteriaDTO::toDto)
                        .toList())
                .subTasks(subTasks)
                .build();
    }

    public TaskSummaryDTO toTaskSummaryDto(Task task) {
        Long taskId = task.getId();
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTask_Id(taskId);
        List<Long> assigneeIds = assignments.stream()
                .map(TaskAssignment::getAssigneeId)
                .toList();

        List<TaskSummaryDTO> subTasks = taskRepository.findSubtasksByParentTaskId(taskId).stream()
                .map(this::toTaskSummaryDto)
                .toList();

        return TaskSummaryDTO.builder()
                .id(taskId)
                .name(task.getName())
                .description(task.getDescription())
                .phaseName(task.getPhase() != null ? task.getPhase().getName() : null)
                .endDate(task.getEndDate())
                .assigneeIds(assigneeIds)
                .assigneeNames(List.of())
                .subTasks(subTasks)
                .build();
    }
}
