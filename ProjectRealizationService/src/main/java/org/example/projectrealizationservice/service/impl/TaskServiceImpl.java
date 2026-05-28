package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectTaskDTO;
import org.example.projectrealizationservice.dto.TaskSummaryDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.mapper.TaskViewMapper;
import org.example.projectrealizationservice.model.neo4j.Task;
import org.example.projectrealizationservice.model.sql.Project;
import org.example.projectrealizationservice.model.sql.ProjectTask;
import org.example.projectrealizationservice.repository.neo4j.TaskRepository;
import org.example.projectrealizationservice.repository.sql.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.sql.ProjectRepository;
import org.example.projectrealizationservice.repository.sql.ProjectTaskRepository;
import org.example.projectrealizationservice.repository.sql.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.sql.TaskResourceAssignmentRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.TaskService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final TaskViewMapper taskViewMapper;
    
    private CacheManager cacheManager;

    @Override
    @Transactional(transactionManager = "neo4jTransactionManager")
    public void createTask(TaskCreationDTO taskCreation) {
        Project project = findAccessibleProjectOrThrow(taskCreation.getProjectId());
        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        Task task = Task.builder()
                .name(taskCreation.getName())
                .description(taskCreation.getDescription())
                .creatorId(creatorId)
                .projectId(project.getId())
                .phaseChangeDate(OffsetDateTime.now())
                .build();
        task = taskRepository.save(task);

        projectTaskRepository.save(ProjectTask.builder()
                .projectId(project.getId())
                .taskId(task.getId())
                .startDate(OffsetDateTime.now())
                .endDate(taskCreation.getEndDate())
                .build());
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void updateTask(String taskId, TaskCreationDTO taskCreation) {
        Task existing = findAccessibleTaskOrThrow(taskId);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());

        existing.setName(taskCreation.getName());
        existing.setDescription(taskCreation.getDescription());
        taskRepository.save(existing);

        if (taskCreation.getEndDate() != null) {
            projectTaskRepository.findByTaskId(taskId).ifPresent(projectTask -> {
                projectTask.setEndDate(taskCreation.getEndDate());
                projectTaskRepository.save(projectTask);
            });
        }
        Objects.requireNonNull(cacheManager.getCache("tasks-summary")).evict(existing.getProjectId());
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void deleteTask(String taskId) {
        Task existing = findAccessibleTaskOrThrow(taskId);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());

        acceptanceCriteriaRepository.deleteAll(acceptanceCriteriaRepository.findByTaskId(taskId));
        taskAssignmentRepository.deleteAll(taskAssignmentRepository.findByTaskId(taskId));
        taskResourceAssignmentRepository.deleteAll(taskResourceAssignmentRepository.findByTaskId(taskId));
        projectTaskRepository.findByTaskId(taskId)
                .ifPresent(projectTaskRepository::delete);
        taskRepository.delete(existing);

        Objects.requireNonNull(cacheManager.getCache("tasks-summary")).evict(existing.getProjectId());
    }

    @Override
    @Cacheable(value = "tasks-summary", key = "#projectId", condition = "#projectId != null")
    public List<TaskSummaryDTO> getTasksByProjectId(String projectId) {
        Project project = findAccessibleProjectOrThrow(projectId);
        return taskRepository.findByProjectId(project.getId()).stream()
                .filter(task -> task.getParentTask() == null)
                .map(taskViewMapper::toTaskSummaryDto)
                .toList();
    }

    @Override
    public ProjectTaskDTO getTaskById(String taskId) {
        return taskViewMapper.toProjectTaskDto(findAccessibleTaskOrThrow(taskId));
    }

    private Task findAccessibleTaskOrThrow(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
        if (task.getProjectId() != null) {
            findAccessibleProjectOrThrow(String.valueOf(task.getProjectId()));
        }
        return task;
    }

    private Project findAccessibleProjectOrThrow(String projectId) {
        Project project = projectRepository.findById(Long.parseLong(projectId))
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
        if (!Objects.equals(project.getCreatorId(), SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("You do not have access to this project.");
        }
        return project;
    }
}
