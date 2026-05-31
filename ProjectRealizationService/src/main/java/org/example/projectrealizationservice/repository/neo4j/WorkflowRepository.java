package org.example.projectrealizationservice.repository.neo4j;

import org.example.projectrealizationservice.model.neo4j.Phase;
import org.example.projectrealizationservice.model.neo4j.Workflow;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("""
        MATCH (t:Task)-[:USES_WORKFLOW]->(w:Workflow)
        WHERE t.projectId = $projectId
        WITH DISTINCT w, count(t) AS taskCount
        WHERE taskCount >= 2
        ORDER BY taskCount DESC
        RETURN w
        """)
    List<Workflow> findRecommendedWorkflowsForProject(Long projectId);
}
