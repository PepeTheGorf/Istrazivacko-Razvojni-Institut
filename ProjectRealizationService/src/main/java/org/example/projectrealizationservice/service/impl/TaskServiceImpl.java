package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.AcceptanceCriteriaDTO;
import org.example.projectrealizationservice.dto.ProjectTaskDTO;
import org.example.projectrealizationservice.dto.TechnicalResourceDTO;
import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.model.*;
import org.example.projectrealizationservice.repository.*;
import org.example.projectrealizationservice.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    public void createTask(TaskCreationDTO taskCreation) {
        Project project = projectRepository.findById(taskCreation.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
        
        Task task = Task.builder()
                .name(taskCreation.getName())
                .description(taskCreation.getDescription())
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

        existing.setName(taskCreation.getName());
        existing.setDescription(taskCreation.getDescription());
        if (taskCreation.getEndDate() != null) {
            taskRepository.createOrUpdateTaskDates(taskCreation.getProjectId(), taskId, taskCreation.getEndDate());
        }
        taskRepository.save(existing);
    }

    @Override
    public void deleteTask(String taskId) {
            Task existing = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
            taskRepository.delete(existing);
    }

    @Override
    public void moveTaskToNextPhase(String taskId) {

    }

    @Override
    public void completeAcceptanceCriteria(String taskId, String criteriaId) {

    }

    @Override
    public List<ProjectTaskDTO> getTasksByProjectId(String projectId) {
        return taskRepository.findAllByProject(projectId).stream()
                .map(task -> {
                    List<TechnicalResourceDTO> technicalResources = task.getTechnicalResources().stream()
                            .map(tech -> TechnicalResourceDTO.toDto(tech.getTechnicalResource()))
                            .toList();
                    List<AcceptanceCriteriaDTO> acceptanceCriteria = task.getAcceptanceCriteria().stream()
                            .map(AcceptanceCriteriaDTO::toDto)
                            .toList();
                    WorkflowDTO workflow = WorkflowDTO.toDTO(
                            task.getWorkflow()
                    );
                    
                    return ProjectTaskDTO.builder()
                            .id(task.getId())
                            .name(task.getName())
                            .description(task.getDescription())
                            .workflow(workflow)
                            .technicalResources(technicalResources)
                            .acceptanceCriteria(acceptanceCriteria)
                            .build();
                })
                .toList();
    }

    //todo: create dto toDto method for task :)
    @Override
    public ProjectTaskDTO getTaskById(String taskId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    List<TechnicalResourceDTO> technicalResources = task.getTechnicalResources().stream()
                            .map(tech -> TechnicalResourceDTO.toDto(tech.getTechnicalResource()))
                            .toList();
                    List<AcceptanceCriteriaDTO> acceptanceCriteria = task.getAcceptanceCriteria().stream()
                            .map(AcceptanceCriteriaDTO::toDto)
                            .toList();
                    WorkflowDTO workflow = WorkflowDTO.toDTO(
                            task.getWorkflow()
                    );
                    
                    return ProjectTaskDTO.builder()
                            .id(task.getId())
                            .name(task.getName())
                            .description(task.getDescription())
                            .workflow(workflow)
                            .technicalResources(technicalResources)
                            .acceptanceCriteria(acceptanceCriteria)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
    }


    @Override
    public List<AcceptanceCriteria> analyzeAcceptanceCriteriaCompletion(String projectId, String phaseId, long minCompleted) {
        return List.of();
    }
}
