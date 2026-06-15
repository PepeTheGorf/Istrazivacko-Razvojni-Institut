package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "smart_document_categories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}