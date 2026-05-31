package org.example.projectrealizationservice.repository.sql.smartdocs;

import org.example.projectrealizationservice.model.sql.smartdocs.DocumentSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentSectionRepository extends JpaRepository<DocumentSection, Long> {
}