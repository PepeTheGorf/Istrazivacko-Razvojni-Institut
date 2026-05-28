package org.example.projectrealizationservice.mapper;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.*;
import org.example.projectrealizationservice.model.neo4j.Task;
import org.example.projectrealizationservice.model.sql.ProjectTask;
import org.example.projectrealizationservice.model.sql.TaskAssignment;
import org.example.projectrealizationservice.model.sql.TaskResourceAssignment;
import org.example.projectrealizationservice.model.sql.TechnicalResource;
import org.example.projectrealizationservice.repository.neo4j.TaskRepository;
import org.example.projectrealizationservice.repository.sql.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.sql.ProjectTaskRepository;
import org.example.projectrealizationservice.repository.sql.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.sql.TaskResourceAssignmentRepository;
import org.example.projectrealizationservice.repository.sql.TechnicalResourceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskViewMapper {

    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final TechnicalResourceRepository technicalResourceRepository;
    private final TaskRepository taskRepository;
    private final ProjectTaskRepository projectTaskRepository;

    public ProjectTaskDTO toProjectTaskDto(Task task) {
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskId(task.getId());
        List<TaskResourceAssignment> resourceAssignments =
                taskResourceAssignmentRepository.findByTaskId(task.getId());

        Map<Long, TechnicalResource> resourcesById = technicalResourceRepository.findAllById(
                        resourceAssignments.stream()
                                .map(TaskResourceAssignment::getTechnicalResourceId)
                                .distinct()
                                .toList())
                .stream()
                .collect(Collectors.toMap(TechnicalResource::getId, Function.identity()));

        List<TechnicalResourceDTO> technicalResources = resourceAssignments.stream()
                .map(assignment -> {
                    TechnicalResource resource = resourcesById.get(assignment.getTechnicalResourceId());
                    return resource != null ? TechnicalResourceDTO.toDto(resource) : null;
                })
                .filter(dto -> dto != null)
                .toList();

        Long assigneeId = assignments.stream()
                .map(TaskAssignment::getAssigneeId)
                .findFirst()
                .orElse(null);

        List<ProjectTaskDTO> subTasks = taskRepository.findSubtasksByParentTaskId(task.getId()).stream()
                .map(this::toProjectTaskDto)
                .toList();

        ProjectTask projectTask = projectTaskRepository.findByTaskId(task.getId()).orElse(null);

        return ProjectTaskDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .phaseName(task.getPhase() != null ? task.getPhase().getName() : null)
                .startDate(projectTask != null ? projectTask.getStartDate() : null)
                .endDate(projectTask != null ? projectTask.getEndDate() : null)
                .creatorId(task.getCreatorId())
                .assigneeId(assigneeId)
                .workflow(WorkflowDTO.toDTO(task.getWorkflow()))
                .technicalResources(technicalResources)
                .acceptanceCriteria(acceptanceCriteriaRepository.findByTaskId(task.getId()).stream()
                        .map(AcceptanceCriteriaDTO::toDto)
                        .toList())
                .subTasks(subTasks)
                .build();
    }

    public TaskSummaryDTO toTaskSummaryDto(Task task) {
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskId(task.getId());
        
        //todo: fix this later maybe by calling stakeholders api?
        List<Long> assigneeId = assignments.stream()
                .map(TaskAssignment::getAssigneeId).toList();

        List<TaskSummaryDTO> subTasks = taskRepository.findSubtasksByParentTaskId(task.getId()).stream()
                .map(this::toTaskSummaryDto)
                .toList();

        
        //Move this data into task I think
        ProjectTask projectTask = projectTaskRepository.findByTaskId(task.getId()).orElse(null);

        return TaskSummaryDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .phaseName(task.getPhase() != null ? task.getPhase().getName() : null)
                .endDate(projectTask != null ? projectTask.getEndDate() : null)
                .assigneeNames(List.of())
                .subTasks(subTasks)
                .build();
    }
}
