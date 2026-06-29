package org.example.projectrealizationservice.dto.smartdocs;

import lombok.Data;
import java.util.List;

@Data
public class TemplateCreationDTO {
    private String name;
    private String description;
    private Long domainId;     
    private String newDomain;  
    private Long categoryId;   
    private String newCategory;
    private List<SectionDTO> sections;

    @Data
    public static class SectionDTO {
        private String title;
        private String systemPrompt;
        private Integer order;
    }
}