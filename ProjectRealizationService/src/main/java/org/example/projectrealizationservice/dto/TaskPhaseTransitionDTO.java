package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.example.projectrealizationservice.dto.TransitionRequirementStatusDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPhaseTransitionDTO {
    private Long toPhaseId;
    private String toPhaseName;
    private boolean routeExists;
    private boolean conditionsMet;
    private List<TransitionRequirementStatusDTO> requirements;
}
