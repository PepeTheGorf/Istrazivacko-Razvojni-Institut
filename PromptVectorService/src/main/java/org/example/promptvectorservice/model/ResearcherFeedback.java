package org.example.promptvectorservice.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ResearcherFeedback {
    private Long id;
    private Integer rating;            
    private Integer regenerationCount; 
    private String sectionName;         
    private List<Float> feedbackVector; 
}