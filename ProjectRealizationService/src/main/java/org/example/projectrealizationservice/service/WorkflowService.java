package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;

public interface WorkflowService {
    void createWorkflow(WorkflowCreationDTO workflow);
    void updateWorkflow(String workflowId, WorkflowCreationDTO workflow);
    void deleteWorkflow(String workflowId);

    WorkflowDTO getWorkflowById(String workflowId);
    WorkflowDTO getWorkflowByName(String name);
}
