package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.Workflow;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowRepository extends Neo4jRepository<Workflow, String> {
    Optional<Workflow> findByName(String name);
    
    @Query("""
            MATCH (w:Workflow)-[:HAS_PHASE]->(p:Phase)
            WHERE elementId(w) = $workflowId AND p.order = $currentPhaseOrder + 1
            RETURN p
            """)
    Optional<Phase> findNextPhaseInWorkflow(String workflowId, int currentPhaseOrder);
}
