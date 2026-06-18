package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.TaskResourceAssignment;
import org.example.projectrealizationservice.model.TechnicalResource;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResourceAssignmentDTO {
    private Long resourceId;
    private String name;
    private String description;
    private Integer assignedQuantity;
    private Integer availableQuantity;

    public static TaskResourceAssignmentDTO fromAssignment(TaskResourceAssignment assignment, TechnicalResource resource) {
        return TaskResourceAssignmentDTO.builder()
                .resourceId(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .assignedQuantity(assignment.getQuantity())
                .availableQuantity(resource.getQuantity())
                .build();
    }
}
