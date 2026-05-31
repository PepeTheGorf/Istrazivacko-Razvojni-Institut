package org.example.elasticsearchservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratedSection {
    private String id;
    private String sectionName;
    private String generatedText;
    private String researchField;
    private Float confidenceScore;
    private Integer generationCount;
    private Long createdAt;
    private String status; // DRAFT, APPROVED, REJECTED
}
