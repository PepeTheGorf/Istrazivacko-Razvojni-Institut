package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;
import org.example.projectrealizationservice.model.neo4j.Phase;
import org.example.projectrealizationservice.model.neo4j.Workflow;
import org.example.projectrealizationservice.repository.neo4j.WorkflowRepository;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.WorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "neo4jTransactionManager")
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;

    @Override
    public void createWorkflow(WorkflowCreationDTO workflow) {
        Workflow existing = workflowRepository.findByName(workflow.getName()).orElse(null);
        if (existing != null) {
            throw new RuntimeException("Workflow with the same name already exists!");
        }

        if (workflow.getPhases() == null || workflow.getPhases().size() < 2) {
            throw new RuntimeException("Workflow must contain at least two phases!");
        }

        Long creatorId = SecurityUtils.getCurrentUserId();
        if (creatorId == null) {
            throw new RuntimeException("Unauthenticated user cannot create workflows.");
        }

        Set<Phase> phases = workflow.getPhases().stream()
                .map(dto -> Phase.builder()
                        .name(dto.getName())
                        .order(dto.getOrder())
                        .build())
                .collect(Collectors.toSet());
        //todo: transition conditions

        Workflow toSave = Workflow.builder()
                .name(workflow.getName())
                .description(workflow.getDescription())
                .creatorId(creatorId)
                .phases(phases)
                .build();

        workflowRepository.save(toSave);
    }

    @Override
    public void updateWorkflow(String workflowId, WorkflowCreationDTO workflow) {
        Workflow existing = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));

        if (workflow.getPhases() == null || workflow.getPhases().size() < 2) {
            throw new RuntimeException("Workflow must contain at least two phases!");
        }

        Set<Phase> phases = workflow.getPhases().stream()
                .map(dto -> Phase.builder()
                        .name(dto.getName())
                        .order(dto.getOrder())
                        .build())
                .collect(Collectors.toSet());

        existing.setName(workflow.getName());
        existing.setDescription(workflow.getDescription());
        existing.setPhases(phases);

        workflowRepository.save(existing);
    }

    @Override
    public void deleteWorkflow(String workflowId) {
        Workflow existing = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));
        workflowRepository.delete(existing);
    }

    @Override
    public List<WorkflowDTO> getAllWorkflows() {
        return workflowRepository.findAll().stream()
                .map(WorkflowDTO::toDTO)
                .sorted(Comparator.comparing(WorkflowDTO::getName))
                .collect(Collectors.toList());
    }

    @Override
    public WorkflowDTO getWorkflowByName(String name) {
        Workflow workflow = workflowRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Workflow with that name does not exist!"));
        return WorkflowDTO.toDTO(workflow);
    }
}
