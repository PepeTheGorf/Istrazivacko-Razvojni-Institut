package org.example.promptvectorservice.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    private Long id;
    private String category;      
    private Float avg_rating;    
    private Integer version;
    private String prompt_text;
    private String short_summary;       
    private List<Float> prompt_vector; 
    private List<Float> summary_vector;
}