package org.example.projectrealizationservice.dto.smartdocs;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class SmartDocumentSummaryDTO {
    private Long id;
    private String templateName;
    private String status;
    private OffsetDateTime createdAt;
    private Integer progress; 
}