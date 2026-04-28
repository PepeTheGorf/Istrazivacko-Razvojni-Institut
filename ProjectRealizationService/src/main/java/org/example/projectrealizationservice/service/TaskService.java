package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.ProjectTaskDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.model.AcceptanceCriteria;

import java.util.List;

public interface TaskService {
    void createTask(TaskCreationDTO taskCreation);
    void createSubtask(String parentTaskId, TaskCreationDTO taskCreation);

    ProjectTaskDTO getTaskById(String taskId);

    void updateTask(String taskId, TaskCreationDTO taskCreation);
    void deleteTask(String taskId);

    void moveTaskToNextPhase(String taskId);
    void completeAcceptanceCriteria(String taskId, String criteriaId);

    List<ProjectTaskDTO> getTasksByProjectId(String projectId);

    List<AcceptanceCriteria> analyzeAcceptanceCriteriaCompletion(String projectId, String phaseId, long minCompleted);
}
