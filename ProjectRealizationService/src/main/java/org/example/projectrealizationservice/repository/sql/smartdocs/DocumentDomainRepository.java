package org.example.projectrealizationservice.repository.sql.smartdocs;

import org.example.projectrealizationservice.model.sql.smartdocs.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentDomainRepository extends JpaRepository<DocumentDomain, Long> {
    Optional<DocumentDomain> findByName(String name);
}