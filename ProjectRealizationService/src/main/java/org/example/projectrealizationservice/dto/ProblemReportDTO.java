package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.ProblemReport;
import org.example.projectrealizationservice.model.ProblemStatus;
import org.example.projectrealizationservice.model.ProblemType;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReportDTO {
    private String id;
    private Long creatorId;
    private Long reviewedById;
    private String description;
    private ProblemType problemType;
    private ProblemStatus status;
    private OffsetDateTime reportedAt;

    public static ProblemReportDTO toDTO(ProblemReport problemReport) {
        if (problemReport == null) {
            return null;
        }
        return ProblemReportDTO.builder()
                .id(problemReport.getId())
                .creatorId(problemReport.getCreatorId())
                .reviewedById(problemReport.getReviewedById())
                .description(problemReport.getDescription())
                .problemType(problemReport.getProblemType())
                .status(problemReport.getStatus())
                .reportedAt(problemReport.getReportedAt())
                .build();
    }
}
