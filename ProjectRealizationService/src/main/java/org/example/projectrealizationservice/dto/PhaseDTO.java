package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.neo4j.Phase;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseDTO {
    private String id;
    private String name;
    private Integer order;

    public static PhaseDTO toDTO(Phase phase) {
        if (phase == null) {
            return null;
        }
        return PhaseDTO.builder()
                .id(phase.getId())
                .name(phase.getName())
                .order(phase.getOrder())
                .build();
    }
}
