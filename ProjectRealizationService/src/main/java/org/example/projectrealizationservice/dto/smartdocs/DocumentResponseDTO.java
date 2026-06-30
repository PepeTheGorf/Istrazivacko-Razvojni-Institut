package org.example.projectrealizationservice.dto.smartdocs;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DocumentResponseDTO {
    private Long id;
    private String status;
    private String templateName;
    private List<SectionResponseDTO> sections;

    @Data
    @Builder
    public static class SectionResponseDTO {
        private Long id;
        private String title; 
        private String userInput;
        private String llmResult;
        private Integer rating;
        private String feedbackComment;
        String refinedResult;
    }
}