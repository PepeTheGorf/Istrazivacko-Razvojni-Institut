package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.TechnicalResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechnicalResourceRepository extends JpaRepository<TechnicalResource, Long> {

    Optional<TechnicalResource> findByName(String name);

    boolean existsByName(String name);
}
