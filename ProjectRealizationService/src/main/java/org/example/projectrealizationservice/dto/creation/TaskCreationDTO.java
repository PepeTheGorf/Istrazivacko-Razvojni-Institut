package org.example.projectrealizationservice.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreationDTO {
    private String name;
    private String description;
    private Long projectId;
    private Long parentTaskId;
    private Long workflowId;
    private Long assigneeId;
    private java.time.OffsetDateTime startDate;
    private java.time.OffsetDateTime endDate;
}
