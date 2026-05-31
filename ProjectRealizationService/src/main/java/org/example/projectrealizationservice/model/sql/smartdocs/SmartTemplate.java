package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "smart_templates")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SmartTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "domain_id")
    private DocumentDomain domain;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private DocumentCategory category;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<TemplateSection> sections;

    private Long creatorId; 
    private OffsetDateTime createdAt;
}