package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectTaskDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.model.*;
import org.example.projectrealizationservice.repository.*;
import org.example.projectrealizationservice.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkflowRepository workflowRepository;
    private final TechnicalResourceRepository technicalResourceRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;


    @Override
    public void moveTaskToNextPhase(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id not found!"));

        Phase currentPhase = task.getPhase();
        Workflow workflow = task.getWorkflow();

        Phase nextPhase = workflow.getPhases().stream()
                .filter(phase -> phase.getOrder() == currentPhase.getOrder() + 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Task is already in the last phase of the workflow!"));

        task.setPhase(nextPhase);
        taskRepository.save(task);
    }

    @Override
    public void completeAcceptanceCriteria(String taskId, String criteriaId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id not found!"));
        AcceptanceCriteria acceptanceCriteria = task.getAcceptanceCriteria()
                .stream()
                .filter(ac -> ac.getId().equalsIgnoreCase(criteriaId) && !ac.isCompleted())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Acceptance criteria with that id not found in the task!"));

        acceptanceCriteria.setCompleted(true);
        taskRepository.save(task);
    }


    @Override
    @Transactional
    public void createTask(TaskCreationDTO taskCreation) {
        projectRepository.findById(taskCreation.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project with that id not found!"));

        Task parentTask = taskCreation.getParentTaskId() == null
                ? null
                : taskRepository.findById(taskCreation.getParentTaskId()).orElse(null);

        Workflow workflow = workflowRepository.findByName(taskCreation.getWorkflow())
                .orElseThrow(() -> new RuntimeException("Workflow with that name not found!"));
        Phase initialPhase = workflow.getPhases().stream()
                .filter(phase -> phase.getOrder() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Workflow does not have an initial phase!"));

        Set<TechnicalResource> technicalResources = taskCreation.getAssignedResources().stream()
                .map(technicalResourceDTO -> technicalResourceRepository.findByName(technicalResourceDTO.getName())
                        .orElseThrow(() -> new RuntimeException("Technical resource with name " + technicalResourceDTO.getName() + " not found!")))
                .collect(Collectors.toSet());

        Set<AcceptanceCriteria> acceptanceCriteria = taskCreation.getAcceptanceCriteria().stream()
                .map(acceptanceCriteriaCreationDTO -> AcceptanceCriteria.builder()
                        .name(acceptanceCriteriaCreationDTO.getName())
                        .description(acceptanceCriteriaCreationDTO.getDescription())
                        .completed(false)
                        .build())
                .collect(Collectors.toSet());
        acceptanceCriteria = new HashSet<>(acceptanceCriteriaRepository.saveAll(acceptanceCriteria));

        Task task = Task.builder()
                .name(taskCreation.getName())
                .description(taskCreation.getDescription())
                .parentTask(parentTask)
                .workflow(workflow)
                .phase(initialPhase)
                .technicalResources(technicalResources)
                .acceptanceCriteria(acceptanceCriteria)
                .build();

        Task saved = taskRepository.save(task);
        taskRepository.linkTaskToProject(taskCreation.getProjectId(), saved.getId(), null, taskCreation.getEndDate());
    }

    @Override
    public void createSubtask(String parentTaskId, TaskCreationDTO taskCreation) {
        String projectId = projectRepository.findProjectIdByTaskId(parentTaskId)
                .orElseThrow(() -> new RuntimeException("Can't create subtask because parent task isn't linked to any project!"));

        TaskCreationDTO subtask = withParentAndProject(taskCreation, parentTaskId, projectId);
        createTask(subtask);
    }

    private TaskCreationDTO withParentAndProject(TaskCreationDTO original, String parentTaskId, String projectId) {
        return TaskCreationDTO.builder()
                .name(original.getName())
                .description(original.getDescription())
                .endDate(original.getEndDate())
                .workflow(original.getWorkflow())
                .acceptanceCriteria(original.getAcceptanceCriteria())
                .assignedResources(original.getAssignedResources())
                .parentTaskId(parentTaskId)
                .projectId(projectId)
                .build();
    }

    @Override
    public ProjectTaskDTO getTaskById(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id not found!"));
        return mapToDtoRecursive(task);
    }

    @Override
    public void updateTask(String taskId, TaskCreationDTO taskCreation) {
        Task existing = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id not found!"));

        existing.setName(taskCreation.getName());
        existing.setDescription(taskCreation.getDescription());

        if (taskCreation.getWorkflow() != null) {
            Workflow workflow = workflowRepository.findByName(taskCreation.getWorkflow())
                    .orElseThrow(() -> new RuntimeException("Workflow with that name not found!"));
            existing.setWorkflow(workflow);

            Phase initialPhase = workflow.getPhases().stream()
                    .filter(phase -> phase.getOrder() == 1)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Workflow does not have an initial phase!"));
            existing.setPhase(initialPhase);
        }

        if (taskCreation.getAssignedResources() != null) {
            Set<TechnicalResource> technicalResources = taskCreation.getAssignedResources().stream()
                    .map(technicalResourceDTO -> technicalResourceRepository.findByName(technicalResourceDTO.getName())
                            .orElseThrow(() -> new RuntimeException("Technical resource with name " + technicalResourceDTO.getName() + " not found!")))
                    .collect(Collectors.toSet());
            existing.setTechnicalResources(technicalResources);
        }

        if (taskCreation.getAcceptanceCriteria() != null) {
            Set<AcceptanceCriteria> acceptanceCriteria = taskCreation.getAcceptanceCriteria().stream()
                    .map(acceptanceCriteriaCreationDTO -> AcceptanceCriteria.builder()
                            .name(acceptanceCriteriaCreationDTO.getName())
                            .description(acceptanceCriteriaCreationDTO.getDescription())
                            .completed(false)
                            .build())
                    .collect(Collectors.toSet());
            acceptanceCriteria = new HashSet<>(acceptanceCriteriaRepository.saveAll(acceptanceCriteria));
            existing.setAcceptanceCriteria(acceptanceCriteria);
        }

        Task saved = taskRepository.save(existing);

        if (taskCreation.getEndDate() != null) {
            String projectId = projectRepository.findProjectIdByTaskId(taskId).orElse(null);
            if (projectId != null) {
                taskRepository.createOrUpdateTaskDates(projectId, saved.getId(), taskCreation.getEndDate());
            }
        }
    }

    @Override
    public void deleteTask(String taskId) {
        taskRepository.detachDeleteById(taskId);
    }

    @Override
    public List<ProjectTaskDTO> getTasksByProjectId(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return project.getTasks().stream()
                .map(task -> mapToDtoRecursive(task.getTask()))
                .toList();
    }

    private ProjectTaskDTO mapToDtoRecursive(Task task) {
        ProjectTaskDTO dto = mapToDto(task);

        List<Task> subTasks = taskRepository.findSubtasksByParentTaskId(task.getId());
        List<ProjectTaskDTO> subTaskDtos = subTasks.stream()
                .map(this::mapToDtoRecursive)
                .toList();

        dto.setSubTasks(subTaskDtos);
        return dto;
    }

    private ProjectTaskDTO mapToDto(Task task) {
        return ProjectTaskDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .phaseName(task.getPhase() != null ? task.getPhase().getName() : null)
                .technicalResourceNames(
                        task.getTechnicalResources().stream()
                                .map(TechnicalResource::getName)
                                .toList()
                )
                .acceptanceCriteriaNames(
                        task.getAcceptanceCriteria().stream()
                                .map(AcceptanceCriteria::getName)
                                .toList()
                )
                .subTasks(new ArrayList<>())
                .build();
    }

    @Override
    public List<AcceptanceCriteria> analyzeAcceptanceCriteriaCompletion(String projectId, String phaseId, long minCompleted) {
        List<Task> tasks = taskRepository.findTasksWithMinCompletedAcceptanceCriteriaByProject(projectId, phaseId, minCompleted);
        tasks = tasks.stream().map(task -> taskRepository.findById(task.getId()).get()).toList();

        return tasks.stream()
                .flatMap(task -> task.getAcceptanceCriteria().stream())
                .filter(AcceptanceCriteria::isCompleted)
                .toList();
    }
}
