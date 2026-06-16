package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "smart_prompt_versions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PromptVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Integer versionNumber;

    private boolean active;

    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "template_section_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TemplateSection templateSection;
}