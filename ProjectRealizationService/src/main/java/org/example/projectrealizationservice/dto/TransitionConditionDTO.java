package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.TransitionCondition;
import org.example.projectrealizationservice.model.TransitionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionConditionDTO {
    private String id;
    private String description;
    private TransitionType type;
    private Long creatorId;
    private PhaseDTO fromPhase;
    private PhaseDTO toPhase;

    public static TransitionConditionDTO toDTO(TransitionCondition condition) {
        if (condition == null) {
            return null;
        }
        return TransitionConditionDTO.builder()
                .id(condition.getId())
                .description(condition.getDescription())
                .type(condition.getType())
                .creatorId(condition.getCreatorId())
                .fromPhase(PhaseDTO.toDTO(condition.getFromPhase()))
                .toPhase(PhaseDTO.toDTO(condition.getToPhase()))
                .build();
    }
}
