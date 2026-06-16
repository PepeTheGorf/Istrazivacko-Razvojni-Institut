package org.example.projectrealizationservice.dto.smartdocs;

import lombok.*;
import org.example.projectrealizationservice.model.sql.smartdocs.DocumentCategory;
import org.example.projectrealizationservice.model.sql.smartdocs.DocumentDomain;
import org.example.projectrealizationservice.model.sql.smartdocs.TemplateSection;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartTemplateDTO {
    private Long id;
    private String name;
    private String description;
    private DocumentDomain domain;
    private DocumentCategory category;
    private List<TemplateSection> sections; 
    private OffsetDateTime createdAt;
    private Double averageRating; 
}