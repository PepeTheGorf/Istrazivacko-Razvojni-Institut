package org.example.projectrealizationservice.repository.sql.smartdocs;

import org.example.projectrealizationservice.model.sql.smartdocs.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {
    List<GeneratedDocument> findByResearcherId(Long researcherId);
}