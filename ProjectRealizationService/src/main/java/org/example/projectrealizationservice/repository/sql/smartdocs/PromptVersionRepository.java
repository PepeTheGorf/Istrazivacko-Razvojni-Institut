package org.example.projectrealizationservice.repository.sql.smartdocs;

import org.example.projectrealizationservice.model.sql.smartdocs.PromptVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PromptVersionRepository extends JpaRepository<PromptVersion, Long> {
    List<PromptVersion> findByTemplateSectionIdOrderByVersionNumberDesc(Long sectionId);
}