package org.example.promptvectorservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearcherFeedback {
    private Long id;
    private Integer rating;            
    private Integer regeneration_count; 
    private String research_field;      
    private String feedback_comment; 
    private List<Float> feedback_vector; 
}