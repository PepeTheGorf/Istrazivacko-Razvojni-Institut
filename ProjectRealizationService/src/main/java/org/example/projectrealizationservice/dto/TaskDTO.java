package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.neo4j.Task;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private String id;
    private String name;
    private String description;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String phase;

    private WorkflowDTO workflow;
    private Set<TechnicalResourceDTO> assignedResources;
    private Set<AcceptanceCriteriaDTO> acceptanceCriteria;
}
