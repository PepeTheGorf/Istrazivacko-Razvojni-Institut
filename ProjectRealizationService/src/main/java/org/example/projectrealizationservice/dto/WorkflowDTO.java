package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.Workflow;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDTO {
    private String id;
    private String name;
    private String description;
    private Long creatorId;
    private List<PhaseDTO> phases;

    public static WorkflowDTO toDTO(Workflow workflow) {
        if (workflow == null) {
            return null;
        }
        return WorkflowDTO.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .creatorId(workflow.getCreatorId())
                .phases(workflow.getPhases() != null
                        ? workflow.getPhases().stream().map(PhaseDTO::toDTO).toList()
                        : null)
                .build();
    }
}
