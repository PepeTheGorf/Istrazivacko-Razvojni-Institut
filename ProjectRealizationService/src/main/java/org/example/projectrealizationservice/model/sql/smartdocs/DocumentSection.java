package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "smart_document_sections")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private GeneratedDocument document;

    @ManyToOne
    @JoinColumn(name = "template_section_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TemplateSection templateSection; 

    @Column(columnDefinition = "TEXT")
    private String userInput; 

    @Column(columnDefinition = "TEXT")
    private String llmResult; 
}