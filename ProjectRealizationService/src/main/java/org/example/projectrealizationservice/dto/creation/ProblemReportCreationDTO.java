package org.example.projectrealizationservice.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.ProblemStatus;
import org.example.projectrealizationservice.model.ProblemType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReportCreationDTO {
    private Long creatorId;
    private Long reviewedById;
    private String description;
    private ProblemType problemType;
    private ProblemStatus status;
}
