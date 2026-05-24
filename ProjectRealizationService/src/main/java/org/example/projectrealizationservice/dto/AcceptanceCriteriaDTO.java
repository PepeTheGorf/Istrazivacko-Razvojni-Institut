package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.AcceptanceCriteria;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceCriteriaDTO {
    private String id;
    private String name;
    private String description;
    private boolean completed;
    private Long creatorId;

    public static AcceptanceCriteriaDTO toDto(AcceptanceCriteria acceptanceCriteria) {
        return AcceptanceCriteriaDTO.builder()
                .id(acceptanceCriteria.getId())
                .name(acceptanceCriteria.getName())
                .description(acceptanceCriteria.getDescription())
                .completed(acceptanceCriteria.isCompleted())
                .creatorId(acceptanceCriteria.getCreatorId())
                .build();
    }
}
