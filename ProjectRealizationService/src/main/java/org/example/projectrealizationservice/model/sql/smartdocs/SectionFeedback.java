package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "smart_section_feedback")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SectionFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rating; 

    @Column(columnDefinition = "TEXT")
    private String comment;

    @OneToOne
    @JoinColumn(name = "document_section_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DocumentSection section;
}