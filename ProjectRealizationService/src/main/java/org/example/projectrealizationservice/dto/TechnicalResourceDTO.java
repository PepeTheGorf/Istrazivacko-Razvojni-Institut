package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.TechnicalResource;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalResourceDTO {
    private String id;
    private String name;
    private String description;
    private Long creatorId;

    public static TechnicalResourceDTO toDto(TechnicalResource technicalResource) {
        if (technicalResource == null) {
            return null;
        }
        return TechnicalResourceDTO.builder()
                .id(technicalResource.getId())
                .name(technicalResource.getName())
                .description(technicalResource.getDescription())
                .creatorId(technicalResource.getCreatorId())
                .build();
    }
}
