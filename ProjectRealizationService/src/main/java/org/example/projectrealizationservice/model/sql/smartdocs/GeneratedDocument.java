package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "smart_generated_documents")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GeneratedDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private SmartTemplate template;

    private Long researcherId; 
    private OffsetDateTime createdAt;
    private String status; 
    private String description;
    private String name;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSection> sections;
}