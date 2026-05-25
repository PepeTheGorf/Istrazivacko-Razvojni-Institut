package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectTaskDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.model.AcceptanceCriteria;
import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.repository.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.ProjectRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.repository.TechnicalResourceRepository;
import org.example.projectrealizationservice.repository.WorkflowRepository;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkflowRepository workflowRepository;
    private final TechnicalResourceRepository technicalResourceRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;

    @Override
    public void createTask(TaskCreationDTO taskCreation) {
        Project project = projectRepository.findById(taskCreation.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
        
        Long creatorId = taskCreation.getCreatorId() != null
                ? taskCreation.getCreatorId()
                : SecurityUtils.getCurrentUserId();

        Task task = Task.builder()
                .name(taskCreation.getName())
                .description(taskCreation.getDescription())
                .creatorId(creatorId)
                .phaseChangeDate(OffsetDateTime.now())
                .parentTask(null)
                .workflow(null)
                .technicalResources(new HashSet<>())
                .acceptanceCriteria(new HashSet<>())
                .build();
        task = taskRepository.save(task);
        taskRepository.linkTaskToProject(project.getId(), task.getId(), null, taskCreation.getEndDate());
    }

    @Override
    public void createSubtask(String parentTaskId, TaskCreationDTO taskCreation) {

    }
    

    @Override
    public void updateTask(String taskId, TaskCreationDTO taskCreation) {
        Task existing = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
        if(existing.getCreatorId() != null && !existing.getCreatorId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("Only the creator of the task can update it!");
        }
        
        existing.setName(taskCreation.getName());
        existing.setDescription(taskCreation.getDescription());
        if (taskCreation.getCreatorId() != null) {
            existing.setCreatorId(taskCreation.getCreatorId());
        }
        if (taskCreation.getEndDate() != null) {
            taskRepository.createOrUpdateTaskDates(taskCreation.getProjectId(), taskId, taskCreation.getEndDate());
        }
        taskRepository.save(existing);
    }

    @Override
    public void deleteTask(String taskId) {
        Task existing = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
        if(existing.getCreatorId() != null && !existing.getCreatorId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("Only the creator of the task can delete it!");
        }
        
        taskRepository.delete(existing);
    }

    //todo: make this method accept phaseId as well and also check if transition condition exists and is fulfilled before allowing the transition
    @Override
    public void moveTaskToNextPhase(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
        if(task.getAssignments().stream().noneMatch(taskAssignment -> taskAssignment.getAssigneeId().equals(SecurityUtils.getCurrentUserId()))) {
            throw new RuntimeException("Only assigned users can move the task to the next phase!");
        }
        Phase nextPhase = workflowRepository.findNextPhaseInWorkflow(task.getWorkflow().getId(), task.getPhase().getOrder())
                .orElseThrow(() -> new RuntimeException("Task is already in the last phase of the workflow!"));
        task.setPhase(nextPhase);
        task.setPhaseChangeDate(OffsetDateTime.now());
        taskRepository.save(task);
    }

    @Override
    public void completeAcceptanceCriteria(String taskId, String criteriaId) {

    }

    @Override
    public List<ProjectTaskDTO> getTasksByProjectId(String projectId) {
        return taskRepository.findAllByProject(projectId).stream()
                .map(ProjectTaskDTO::fromTask)
                .toList();
    }

    //todo: create dto toDto method for task :)
    @Override
    public ProjectTaskDTO getTaskById(String taskId) {
        return taskRepository.findById(taskId)
                .map(ProjectTaskDTO::fromTask)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
    }


    @Override
    public List<AcceptanceCriteria> analyzeAcceptanceCriteriaCompletion(String projectId, String phaseId, long minCompleted) {
        return List.of();
    }
}
