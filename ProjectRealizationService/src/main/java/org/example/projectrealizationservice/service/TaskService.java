package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.*;
import org.example.projectrealizationservice.dto.creation.TaskAssignmentDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;

import java.util.List;

public interface TaskService {
    TaskSummaryDTO createTask(TaskCreationDTO taskCreation);

    void updateTask(Long taskId, TaskCreationDTO taskCreation);

    void deleteTask(Long taskId);

    void deleteTasksForProject(Long projectId);

    List<TaskSummaryDTO> getTasksByProjectId(Long projectId);
    
    List<AssignedTaskSummaryDTO> getMyTasksByProjectId(Long projectId);

    List<AssignedProjectSummaryDTO> getMyProjects();
    
    TaskTransitionsResponseDTO getTaskTransitions(Long taskId);

    ProjectTaskDTO getTaskById(Long taskId);

    void assignTaskToUser(TaskAssignmentDTO taskAssignmentDTO);

    void moveTaskToNextPhase(Long taskId, Long phaseId);
}
