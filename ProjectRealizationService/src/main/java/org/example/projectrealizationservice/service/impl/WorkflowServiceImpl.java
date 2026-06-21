package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.TransitionConditionTypeDTO;
import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.PhaseCreationDTO;
import org.example.projectrealizationservice.dto.creation.TransitionConditionDTO;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;
import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.TransitionCondition;
import org.example.projectrealizationservice.model.TransitionConditionType;
import org.example.projectrealizationservice.model.Workflow;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.repository.TransitionConditionTypeRepository;
import org.example.projectrealizationservice.repository.WorkflowRepository;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.WorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final TransitionConditionTypeRepository transitionConditionTypeRepository;
    private final TaskRepository taskRepository;

    @Override
    public void createWorkflow(WorkflowCreationDTO workflow) {
        if (workflowRepository.findByName(workflow.getName()).isPresent()) {
            throw new RuntimeException("Workflow with that name already exists!");
        }

        if (workflow.getPhases() == null || workflow.getPhases().size() < 2) {
            throw new RuntimeException("Workflow must contain at least two phases!");
        }

        Long creatorId = SecurityUtils.getCurrentUserId();
        if (creatorId == null) {
            throw new RuntimeException("Unauthenticated user cannot create workflows.");
        }

        Workflow toSave = Workflow.builder()
                .name(workflow.getName())
                .description(workflow.getDescription())
                .creatorId(creatorId)
                .build();

        Set<Phase> phases = workflow.getPhases().stream()
                .map(dto -> Phase.builder()
                        .name(dto.getName())
                        .order(dto.getOrder())
                        .workflow(toSave)
                        .build())
                .collect(Collectors.toSet());
        toSave.setPhases(phases);
        applyTransitionConditions(toSave, phases, workflow.getTransitionConditions());

        workflowRepository.save(toSave);
    }

    @Override
    public void updateWorkflow(Long workflowId, WorkflowCreationDTO workflow) {
        Workflow existing = workflowRepository.findByIdWithPhases(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));

        if (workflow.getPhases() == null || workflow.getPhases().size() < 2) {
            throw new RuntimeException("Workflow must contain at least two phases!");
        }

        existing.setName(workflow.getName());
        existing.setDescription(workflow.getDescription());
        syncPhases(existing, workflow.getPhases());
        applyTransitionConditions(existing, existing.getPhases(), workflow.getTransitionConditions());

        workflowRepository.save(existing);
    }

    private void syncPhases(Workflow existing, List<PhaseCreationDTO> phaseDtos) {
        Map<Long, Phase> existingById = existing.getPhases().stream()
                .filter(phase -> phase.getId() != null)
                .collect(Collectors.toMap(Phase::getId, phase -> phase));

        Set<Long> keptIds = new HashSet<>();

        for (PhaseCreationDTO dto : phaseDtos) {
            if (dto.getId() != null && existingById.containsKey(dto.getId())) {
                Phase phase = existingById.get(dto.getId());
                phase.setName(dto.getName());
                phase.setOrder(dto.getOrder());
                keptIds.add(dto.getId());
            } else {
                existing.getPhases().add(Phase.builder()
                        .name(dto.getName())
                        .order(dto.getOrder())
                        .workflow(existing)
                        .build());
            }
        }

        Iterator<Phase> iterator = existing.getPhases().iterator();
        while (iterator.hasNext()) {
            Phase phase = iterator.next();
            if (phase.getId() == null || keptIds.contains(phase.getId())) {
                continue;
            }
            if (taskRepository.existsByPhase_Id(phase.getId())) {
                throw new RuntimeException(
                        "Cannot remove phase '" + phase.getName() + "' because tasks are using it.");
            }
            iterator.remove();
        }
    }

    private void applyTransitionConditions(
            Workflow workflow,
            Set<Phase> phases,
            List<TransitionConditionDTO> conditionDtos) {
        workflow.getTransitionConditions().clear();

        List<TransitionConditionDTO> dtos = conditionDtos != null ? conditionDtos : List.of();
        for (TransitionConditionDTO transitionConditionDTO : dtos) {
            Phase fromPhase = phases.stream()
                    .filter(phase -> phase.getName().equals(transitionConditionDTO.getFrom()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("From phase with that name does not exist in the workflow!"));
            Phase toPhase = phases.stream()
                    .filter(phase -> phase.getName().equals(transitionConditionDTO.getTo()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("To phase with that name does not exist in the workflow!"));

            List<Long> typeIds = transitionConditionDTO.getTransitionTypeId() != null
                    ? transitionConditionDTO.getTransitionTypeId()
                    : List.of();
            if (typeIds.isEmpty()) {
                throw new RuntimeException("Transition must have at least one condition type!");
            }

            for (Long typeId : typeIds) {
                TransitionConditionType type = transitionConditionTypeRepository.findById(typeId)
                        .orElseThrow(() -> new RuntimeException("Transition condition type with that id does not exist!"));
                workflow.getTransitionConditions().add(TransitionCondition.builder()
                        .workflow(workflow)
                        .transitionType(type)
                        .fromPhase(fromPhase)
                        .toPhase(toPhase)
                        .build());
            }
        }
    }

    @Override
    public void deleteWorkflow(Long workflowId) {
        Workflow existing = workflowRepository.findByIdWithPhases(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));
        if (taskRepository.existsByWorkflow_Id(workflowId)) {
            throw new RuntimeException("Cannot delete workflow because tasks are using it.");
        }
        workflowRepository.delete(existing);
    }

    @Override
    public List<WorkflowDTO> getAllWorkflows() {
        return workflowRepository.findAllWithPhases().stream()
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

    @Override
    public WorkflowDTO getWorkflowById(Long workflowId) {
        Workflow workflow = workflowRepository.findByIdWithDetails(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow with that id does not exist!"));
        return WorkflowDTO.toDTO(workflow, true);
    }

    @Override
    public List<TransitionConditionTypeDTO> getAllTransitionConditionTypes() {
        return transitionConditionTypeRepository.findAll()
                .stream()
                .map(TransitionConditionTypeDTO::toDTO)
                .toList();
    }
}
