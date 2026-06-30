package org.example.projectrealizationservice.dto.smartdocs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalyticsReportDTO {
    private Long templateId;
    private String templateName;
    private Long totalGeneratedSections;
    private Double averageRating;
    private Double avgHumanEditRatio; 
}