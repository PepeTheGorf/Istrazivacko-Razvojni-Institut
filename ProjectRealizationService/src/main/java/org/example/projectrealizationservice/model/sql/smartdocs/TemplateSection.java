package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "smart_template_sections")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String systemPrompt; 

    private Integer sectionOrder;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private SmartTemplate template;
}