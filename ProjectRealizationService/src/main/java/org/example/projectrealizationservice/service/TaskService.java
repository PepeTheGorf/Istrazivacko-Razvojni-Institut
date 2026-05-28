package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.ProjectTaskDTO;
import org.example.projectrealizationservice.dto.TaskSummaryDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;

import java.util.List;

public interface TaskService {
    void createTask(TaskCreationDTO taskCreation);

    void updateTask(String taskId, TaskCreationDTO taskCreation);

    void deleteTask(String taskId);

    List<TaskSummaryDTO> getTasksByProjectId(String projectId);

    ProjectTaskDTO getTaskById(String taskId);
}
