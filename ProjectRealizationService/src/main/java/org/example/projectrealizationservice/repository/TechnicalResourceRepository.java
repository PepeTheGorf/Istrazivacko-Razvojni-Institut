package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.TechnicalResource;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechnicalResourceRepository extends Neo4jRepository<TechnicalResource, String> {
    Optional<TechnicalResource> findByName(String name);
    boolean existsByName(String name);
}
