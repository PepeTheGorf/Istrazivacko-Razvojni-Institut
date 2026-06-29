package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.TransitionConditionTypeDTO;
import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;

import java.util.List;

public interface WorkflowService {
    void createWorkflow(WorkflowCreationDTO workflow);

    void updateWorkflow(Long workflowId, WorkflowCreationDTO workflow);

    void deleteWorkflow(Long workflowId);

    WorkflowDTO getWorkflowByName(String name);

    WorkflowDTO getWorkflowById(Long workflowId);

    List<WorkflowDTO> getAllWorkflows();

    List<TransitionConditionTypeDTO> getAllTransitionConditionTypes();
}
