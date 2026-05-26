package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.ProblemStatus;
import org.example.projectrealizationservice.model.ProblemType;
import org.example.projectrealizationservice.model.sql.ProblemReport;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReportDTO {
    private String id;
    private String taskId;
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
                .id(String.valueOf(problemReport.getId()))
                .taskId(problemReport.getTaskId())
                .creatorId(problemReport.getCreatorId())
                .reviewedById(problemReport.getReviewedById())
                .description(problemReport.getDescription())
                .problemType(problemReport.getProblemType())
                .status(problemReport.getStatus())
                .reportedAt(problemReport.getReportedAt())
                .build();
    }
}
