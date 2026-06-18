package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionConditionViewDTO {
    private Long id;
    private String fromPhase;
    private String toPhase;
    
    private TransitionConditionTypeDTO type;
}
