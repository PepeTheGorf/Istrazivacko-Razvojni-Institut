package org.example.projectrealizationservice.repository.sql.smartdocs;

import org.example.projectrealizationservice.model.sql.smartdocs.SmartTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SmartTemplateRepository extends JpaRepository<SmartTemplate, Long> {
    List<SmartTemplate> findByDomainIdAndCategoryId(Long domainId, Long categoryId);
}