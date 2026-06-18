package org.example.projectrealizationservice.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceCriteriaCreationDTO {
    private Long taskId;
    private String name;
    private String description;
}
