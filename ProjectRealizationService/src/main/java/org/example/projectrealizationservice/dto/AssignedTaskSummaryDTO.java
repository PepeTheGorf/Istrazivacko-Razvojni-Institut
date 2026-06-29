package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedTaskSummaryDTO {
    private Long id;
    private String name;
    private String description;
    private String phaseName;
    private java.time.OffsetDateTime endDate;
    private Long projectId;
    private String projectName;
}
