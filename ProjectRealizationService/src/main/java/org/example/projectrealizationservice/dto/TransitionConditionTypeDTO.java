package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.TransitionConditionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionConditionTypeDTO {
    private Long id;
    private String name;
    private String description;

    public static TransitionConditionTypeDTO toDTO(TransitionConditionType transitionConditionType) {
        return TransitionConditionTypeDTO.builder()
                .id(transitionConditionType.getId())
                .name(transitionConditionType.getName())
                .description(transitionConditionType.getDescription())
                .build();
    }
}
