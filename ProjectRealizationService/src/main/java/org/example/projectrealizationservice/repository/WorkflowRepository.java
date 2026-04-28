package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Workflow;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowRepository extends Neo4jRepository<Workflow, String> {
    Optional<Workflow> findByName(String name);
}
