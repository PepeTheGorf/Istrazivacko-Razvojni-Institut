package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.neo4j.TransitionCondition;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionConditionDTO {
    private String id;
    private String description;
    private String transitionType;
    private PhaseDTO fromPhase;
    private PhaseDTO toPhase;

    public static TransitionConditionDTO toDTO(TransitionCondition condition) {
        if (condition == null) {
            return null;
        }
        return TransitionConditionDTO.builder()
                .id(condition.getId())
                .description(condition.getDescription())
                .transitionType(condition.getTransitionType())
                .fromPhase(PhaseDTO.toDTO(condition.getFromPhase()))
                .toPhase(PhaseDTO.toDTO(condition.getToPhase()))
                .build();
    }
}
