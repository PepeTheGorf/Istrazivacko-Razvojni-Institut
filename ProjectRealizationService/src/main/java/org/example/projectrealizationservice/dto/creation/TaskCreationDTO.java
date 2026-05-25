package org.example.projectrealizationservice.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreationDTO {
    private String name;
    private String description;
    private OffsetDateTime endDate;
    private String projectId;
    private Long creatorId;
    private Long assigneeId;
}
