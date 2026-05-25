package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;

import java.util.List;

public interface WorkflowService {
    void createWorkflow(WorkflowCreationDTO workflow);
    void updateWorkflow(String workflowId, WorkflowCreationDTO workflow);
    void deleteWorkflow(String workflowId);

    WorkflowDTO getWorkflowByName(String name);
    List<WorkflowDTO> getAllWorkflows();
}
