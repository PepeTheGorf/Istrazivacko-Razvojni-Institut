package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTransitionsResponseDTO {
    private Long currentPhaseId;
    private String currentPhaseName;
    private List<PhaseDTO> workflowPhases;
    private List<TaskPhaseTransitionDTO> transitions;
}
