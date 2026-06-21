package org.example.projectrealizationservice.dto.smartdocs;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class ReportResponseDTO {
    private Map<String, Long> requestsPerResearcher;
    
    private String semanticSearchSample; 
    
    private List<TemplateEfficiencyDTO> templateEfficiencies;

    @Data
    @Builder
    public static class TemplateEfficiencyDTO {
        private String templateName;
        private Double averageEffectiveness;
    }
}