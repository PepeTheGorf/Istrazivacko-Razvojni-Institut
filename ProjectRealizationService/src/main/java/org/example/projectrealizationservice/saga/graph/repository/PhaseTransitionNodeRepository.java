package org.example.projectrealizationservice.saga.graph.repository;

import org.example.projectrealizationservice.saga.graph.model.PhaseTransitionNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PhaseTransitionNodeRepository extends Neo4jRepository<PhaseTransitionNode, String> {
}
