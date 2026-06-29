package org.example.projectrealizationservice.model.sql.smartdocs;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "smart_template_sections")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; 

    private Integer sectionOrder;

    @ManyToOne
    @JoinColumn(name = "template_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private SmartTemplate template;

    @OneToMany(mappedBy = "templateSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromptVersion> promptVersions;

    public String getActivePromptContent() {
        if (promptVersions == null) return "";
        return promptVersions.stream()
                .filter(PromptVersion::isActive)
                .map(PromptVersion::getContent)
                .findFirst()
                .orElse("");
    }
}