package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.Workflow;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDTO {
    private Long id;
    private String name;
    private String description;
    private Long creatorId;
    private List<PhaseDTO> phases;
    private List<WorkflowTransitionConditionDTO> transitionConditions;

    public static WorkflowDTO toDTO(Workflow workflow) {
        return toDTO(workflow, false);
    }

    public static WorkflowDTO toDTO(Workflow workflow, boolean includeTransitionConditions) {
        if (workflow == null) {
            return null;
        }
        WorkflowDTO.WorkflowDTOBuilder builder = WorkflowDTO.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .creatorId(workflow.getCreatorId())
                .phases(workflow.getPhases() != null
                        ? workflow.getPhases().stream().map(PhaseDTO::toDTO).toList()
                        : null);
        if (includeTransitionConditions && workflow.getTransitionConditions() != null) {
            builder.transitionConditions(groupTransitionConditions(workflow.getTransitionConditions()));
        }
        return builder.build();
    }

    private static List<WorkflowTransitionConditionDTO> groupTransitionConditions(
            java.util.Set<org.example.projectrealizationservice.model.TransitionCondition> conditions) {
        java.util.Map<String, WorkflowTransitionConditionDTO> grouped = new java.util.LinkedHashMap<>();
        for (org.example.projectrealizationservice.model.TransitionCondition condition : conditions) {
            if (condition.getFromPhase() == null || condition.getToPhase() == null
                    || condition.getTransitionType() == null) {
                continue;
            }
            String key = condition.getFromPhase().getName() + "->" + condition.getToPhase().getName();
            grouped.computeIfAbsent(key, ignored -> WorkflowTransitionConditionDTO.builder()
                    .from(condition.getFromPhase().getName())
                    .to(condition.getToPhase().getName())
                    .transitionTypeId(new java.util.ArrayList<>())
                    .build())
                    .getTransitionTypeId()
                    .add(condition.getTransitionType().getId());
        }
        return new java.util.ArrayList<>(grouped.values());
    }
}
