package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.sql.Project;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private String id;
    private String name;
    private String description;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Long managerId;
    private Long creatorId;

    public static ProjectDTO toDTO(Project project) {
        return ProjectDTO.builder()
                .id(String.valueOf(project.getId()))
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .managerId(project.getCreatorId())
                .creatorId(project.getCreatorId())
                .build();
    }
}
