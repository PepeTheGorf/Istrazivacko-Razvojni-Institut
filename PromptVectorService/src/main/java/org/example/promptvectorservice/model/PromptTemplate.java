package org.example.promptvectorservice.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PromptTemplate {
    private Long id;
    private String templateName;   
    private String category;      
    private Float averageRating;    
    private Integer version;       
    private List<Float> promptVector; 
}