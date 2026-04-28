package org.example.projectrealizationservice.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.dto.TechnicalResourceDTO;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreationDTO {
    private String name;
    private String description;
    private OffsetDateTime endDate;

    // These are fetched from DB
    private String workflow;
    private String projectId;
    private String parentTaskId;
    private List<AcceptanceCriteriaCreationDTO> acceptanceCriteria;
    private List<TechnicalResourceDTO> assignedResources;
}
