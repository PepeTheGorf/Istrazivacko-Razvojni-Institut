package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedTaskSummaryDTO {
    private String id;
    private String name;
    private String description;
    private String phaseName;
    private OffsetDateTime endDate;
    private String projectId;
    private String projectName;
}
