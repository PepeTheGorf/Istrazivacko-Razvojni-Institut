package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.PhaseDTO;
import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;
import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.Workflow;
import org.example.projectrealizationservice.repository.WorkflowRepository;
import org.example.projectrealizationservice.service.WorkflowService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

        Set<Phase> phases = workflow.getPhases().stream()
                .map(dto -> Phase.builder()
                        .name(dto.getName())
                        .order(dto.getOrder())
                        .build())
                .collect(Collectors.toSet());

        Workflow toSave = Workflow.builder()
                .name(workflow.getName())
                .description(workflow.getDescription())
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
    public WorkflowDTO getWorkflowById(String workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));
        return toDto(workflow);
    }

    @Override
    public WorkflowDTO getWorkflowByName(String name) {
        Workflow workflow = workflowRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Workflow with that name does not exist!"));

        return toDto(workflow);
    }

    private WorkflowDTO toDto(Workflow workflow) {
        return WorkflowDTO.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .phases(workflow.getPhases().stream()
                        .sorted(Comparator.comparingInt(Phase::getOrder))
                        .map(phase -> PhaseDTO.builder()
                                .id(phase.getId())
                                .name(phase.getName())
                                .description(null)
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
