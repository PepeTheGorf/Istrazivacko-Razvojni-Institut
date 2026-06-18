package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.*;
import org.example.projectrealizationservice.dto.creation.TaskAssignmentDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.mapper.TaskViewMapper;
import org.example.projectrealizationservice.model.*;
import org.example.projectrealizationservice.repository.*;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.TaskService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final ProblemReportRepository problemReportRepository;
    private final PhaseRepository phaseRepository;
    private final WorkflowRepository workflowRepository;
    private final TransitionConditionRepository transitionConditionRepository;
    
    private final TaskViewMapper taskViewMapper;
    private final CacheManager cacheManager;
    
    private final TransitionConditionEvaluatorImpl transitionConditionEvaluator;

    @Override
    @CacheEvict(value = "tasks-summary", key = "#taskCreation.projectId")
    public TaskSummaryDTO createTask(TaskCreationDTO taskCreation) {
        Project project = findAccessibleProjectOrThrow(taskCreation.getProjectId());
        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        Task.TaskBuilder taskBuilder = Task.builder()
                .name(taskCreation.getName())
                .description(taskCreation.getDescription())
                .creatorId(creatorId)
                .project(project)
                .startDate(OffsetDateTime.now())
                .endDate(taskCreation.getEndDate())
                .phaseChangeDate(OffsetDateTime.now());

        if (taskCreation.getParentTaskId() != null) {
            Task parent = findAccessibleTaskOrThrow(taskCreation.getParentTaskId());
            if (parent.getProject() == null || !Objects.equals(parent.getProject().getId(), project.getId())) {
                throw new RuntimeException("Parent task must belong to the same project.");
            }
            taskBuilder.parentTask(parent);
            Workflow workflow = resolveWorkflow(parent.getWorkflow(), taskCreation.getWorkflowId());
            applyWorkflowAndPhase(taskBuilder, workflow, parent.getPhase());
        } else if (taskCreation.getWorkflowId() != null) {
            Workflow workflow = workflowRepository.findByIdWithPhases(taskCreation.getWorkflowId())
                    .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));
            applyWorkflowAndPhase(taskBuilder, workflow, null);
        }

        Task task = taskRepository.save(taskBuilder.build());
        if (taskCreation.getAssigneeId() != null) {
            assignTaskToUser(TaskAssignmentDTO.builder()
                    .taskId(task.getId())
                    .userId(taskCreation.getAssigneeId())
                    .build());
        }
        return taskViewMapper.toTaskSummaryDto(task);
    }

    @Override
    public void updateTask(Long taskId, TaskCreationDTO taskCreation) {
        Task existing = findAccessibleTaskOrThrow(taskId);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());

        existing.setName(taskCreation.getName());
        existing.setDescription(taskCreation.getDescription());
        if (taskCreation.getEndDate() != null) {
            existing.setEndDate(taskCreation.getEndDate());
        }
        taskRepository.save(existing);
        evictProjectTasksCache(existing);
    }

    @Override
    public void deleteTask(Long taskId) {
        Task existing = findAccessibleTaskOrThrow(taskId);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());

        acceptanceCriteriaRepository.deleteAll(acceptanceCriteriaRepository.findByTask_Id(taskId));
        taskAssignmentRepository.deleteAll(taskAssignmentRepository.findByTask_Id(taskId));
        taskResourceAssignmentRepository.deleteAll(taskResourceAssignmentRepository.findByTask_Id(taskId));
        problemReportRepository.deleteAll(problemReportRepository.findByTask_Id(taskId));
        taskRepository.delete(existing);

        evictProjectTasksCache(existing);
    }
    
    @Override
    public TaskTransitionsResponseDTO getTaskTransitions(Long taskId) {
        Task task = findAccessibleTaskOrThrow(taskId);
    
        Phase currentPhase = task.getPhase();
        Workflow workflow = task.getWorkflow();
    
        if (workflow == null || currentPhase == null) {
            return TaskTransitionsResponseDTO.builder()
                    .currentPhaseId(currentPhase != null ? currentPhase.getId() : null)
                    .currentPhaseName(currentPhase != null ? currentPhase.getName() : null)
                    .workflowPhases(List.of())
                    .transitions(List.of())
                    .build();
        }
    
        List<TransitionCondition> conditions =
                transitionConditionRepository.findByWorkflow(workflow);
    
        List<PhaseDTO> workflowPhases = workflow.getPhases().stream()
                .sorted(Comparator.comparingInt(Phase::getOrder))
                .map(PhaseDTO::toDTO)
                .toList();
    
        List<TaskPhaseTransitionDTO> transitions = workflow.getPhases().stream()
                .filter(phase -> !phase.getId().equals(currentPhase.getId()))
                .sorted(Comparator.comparingInt(Phase::getOrder))
                .map(targetPhase -> {
    
                    List<TransitionCondition> transitionConditions = conditions.stream()
                            .filter(c -> c.getFromPhase().getId().equals(currentPhase.getId()) && c.getToPhase().getId().equals(targetPhase.getId()))
                            .toList();
    
                    boolean transitionExists = !transitionConditions.isEmpty();
    
                    List<TransitionRequirementStatusDTO> requirements = transitionConditions.stream()
                            .filter(c -> c.getTransitionType() != null)
                            .map(c -> TransitionRequirementStatusDTO.builder()
                                    .id(c.getTransitionType().getId())
                                    .name(c.getTransitionType().getName())
                                    .description(c.getTransitionType().getDescription())
                                    .met(transitionConditionEvaluator.evaluateCondition(c, task))
                                    .build())
                            .toList();
    
                    return TaskPhaseTransitionDTO.builder()
                            .toPhaseId(targetPhase.getId())
                            .toPhaseName(targetPhase.getName())
                            .routeExists(transitionExists)
                            .conditionsMet(transitionExists && requirements.stream().allMatch(TransitionRequirementStatusDTO::isMet))
                            .requirements(requirements)
                            .build();
                })
                .toList();
    
        return TaskTransitionsResponseDTO.builder()
                .currentPhaseId(currentPhase.getId())
                .currentPhaseName(currentPhase.getName())
                .workflowPhases(workflowPhases)
                .transitions(transitions)
                .build();
    }

    @Override
    @Cacheable(value = "tasks-summary", key = "#projectId", condition = "#projectId != null")
    public List<TaskSummaryDTO> getTasksByProjectId(Long projectId) {
        Project project = findAccessibleProjectOrThrow(projectId);
        return taskRepository.findRootTasksByProjectId(project.getId()).stream()
                .map(taskViewMapper::toTaskSummaryDto)
                .toList();
    }

    @Override
    public List<AssignedTaskSummaryDTO> getMyTasksByProjectId(Long projectId) {
        Project project = findProjectOrThrow(projectId);
        return taskRepository.findMyTasksByProject(projectId, SecurityUtils.getCurrentUserId())
                .stream()
                .map(task -> AssignedTaskSummaryDTO.builder()
                        .projectName(project.getName())
                        .projectId(projectId)
                        .phaseName(task.getPhase() != null ? task.getPhase().getName() : null)
                        .description(task.getDescription())
                        .name(task.getName())
                        .id(task.getId())
                        .endDate(task.getEndDate())
                        .build())
                .toList();
    }

    @Override
    public List<AssignedProjectSummaryDTO> getMyProjects() {
        Long userId = ResourceAuthorization.requireCurrentUserId();
        return taskRepository.findProjectsForAssignee(userId).stream()
                .map(project -> AssignedProjectSummaryDTO.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .build())
                .toList();
    }

    @Override
    public ProjectTaskDTO getTaskById(Long taskId) {
        return taskViewMapper.toProjectTaskDto(findAccessibleTaskOrThrow(taskId));
    }

    @Override
    public void assignTaskToUser(TaskAssignmentDTO taskAssignmentDTO) {
        Task task = findAccessibleTaskOrThrow(taskAssignmentDTO.getTaskId());
        if (taskAssignmentRepository.existsByTaskAndAssigneeId(task, taskAssignmentDTO.getUserId())) {
            throw new RuntimeException("User is already assigned to this task.");
        }

        taskAssignmentRepository.save(TaskAssignment.builder()
                .task(task)
                .assigneeId(taskAssignmentDTO.getUserId())
                .assignedAt(OffsetDateTime.now())
                .build());
        evictProjectTasksCache(task);
    }

    @Override
    public void moveTaskToNextPhase(Long taskId, Long phaseId) {
        Task task = findAccessibleTaskOrThrow(taskId);
        Phase currentPhase = task.getPhase();
        if (currentPhase == null) {
            throw new RuntimeException("Task has no current phase.");
        }
        if (Objects.equals(currentPhase.getId(), phaseId)) {
            throw new RuntimeException("Task is already in the selected phase.");
        }

        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("Phase with that id does not exist!"));

        List<TransitionCondition> conditions =
                transitionConditionRepository.findByFromPhaseAndToPhase(currentPhase, phase);
        if (!transitionConditionEvaluator.evaluateConditions(conditions, task)) {
            throw new RuntimeException("Transition conditions not met for moving task to the next phase.");
        }

        task.setPhase(phase);
        task.setPhaseChangeDate(OffsetDateTime.now());
        taskRepository.save(task);
    }

    private Task findAccessibleTaskOrThrow(Long taskId) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
        assertCanAccessTask(task);
        return task;
    }

    private void assertCanAccessTask(Task task) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("Unauthenticated user cannot perform this action.");
        }
        if (task.getProject() != null && Objects.equals(task.getProject().getCreatorId(), userId)) {
            return;
        }
        if (taskAssignmentRepository.existsByTaskAndAssigneeId(task, userId)) {
            return;
        }
        throw new RuntimeException("You do not have access to this task.");
    }

    private Project findAccessibleProjectOrThrow(Long projectId) {
        Project project = findProjectOrThrow(projectId);
        if (!Objects.equals(project.getCreatorId(), SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("You do not have access to this project.");
        }
        return project;
    }

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
    }

    private void evictProjectTasksCache(Task task) {
        if (task.getProject() != null) {
            Objects.requireNonNull(cacheManager.getCache("tasks-summary"))
                    .evict(task.getProject().getId());
        }
    }

    private Workflow resolveWorkflow(Workflow parentWorkflow, Long workflowId) {
        if (parentWorkflow != null) {
            return parentWorkflow;
        }
        if (workflowId == null) {
            return null;
        }
        return workflowRepository.findByIdWithPhases(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));
    }

    private void applyWorkflowAndPhase(Task.TaskBuilder taskBuilder, Workflow workflow, Phase parentPhase) {
        if (workflow == null) {
            return;
        }
        taskBuilder.workflow(workflow);
        taskBuilder.phase(resolveInitialPhase(workflow, parentPhase));
    }

    private Phase resolveInitialPhase(Workflow workflow, Phase parentPhase) {
        if (parentPhase != null) {
            return parentPhase;
        }
        return workflow.getPhases().stream()
                .min(Comparator.comparingInt(Phase::getOrder))
                .orElse(null);
    }
}
