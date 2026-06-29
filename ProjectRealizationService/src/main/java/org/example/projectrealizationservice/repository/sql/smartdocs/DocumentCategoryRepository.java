package org.example.projectrealizationservice.repository.sql.smartdocs;

import org.example.projectrealizationservice.model.sql.smartdocs.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {
    Optional<DocumentCategory> findByName(String name);
}