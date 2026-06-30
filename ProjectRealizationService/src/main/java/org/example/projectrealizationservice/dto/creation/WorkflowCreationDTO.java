package org.example.projectrealizationservice.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCreationDTO {
    private String name;
    private String description;
    private Long creatorId;
    private List<PhaseCreationDTO> phases;
    private List<TransitionConditionDTO> transitionConditions;
}
