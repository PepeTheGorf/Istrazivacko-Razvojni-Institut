package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Project;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, String> {
    Optional<Project> findByName(String name);

    @Query("""
        MATCH (p:Project)-[:HAS_TASK]->(t:Task)-[:USES_WORKFLOW]->(w:Workflow)
        WHERE w.name = $workflowName
        WITH p, count(DISTINCT t) AS totalTasksUsingWorkflow
        WHERE totalTasksUsingWorkflow > $minTaskCount
        RETURN DISTINCT p
        """)
    List<Project> findProjectsByWorkflowWithMinTaskCount(
            String workflowName,
            long minTaskCount
    );

    @Query("""
        MATCH (p:Project)-[pt:HAS_TASK]->(t:Task)
        WHERE pt.endDate < $currentDate
        MATCH (t)-[:USES_WORKFLOW]->(w:Workflow)-[:HAS_PHASE]->(phases)
        MATCH (t)-[:IN_PHASE]->(ph:Phase)
        WITH p, t, ph, max(phases.order) AS maxPhaseOrder
        WHERE ph.order <> maxPhaseOrder
        WITH p, count(DISTINCT t) AS delayedTaskCount
        WHERE delayedTaskCount > 0
        RETURN DISTINCT p
        """)
    List<Project> findProjectsWithDelayedTasks(OffsetDateTime currentDate);

    @Query("""
        MATCH (p:Project)-[pt:HAS_TASK]->(t:Task)-[:HAS_TECHNICAL_RESOURCE]->(tr:TechnicalResource)
        WHERE pt.startDate >= $startDate
          AND pt.endDate <= $endDate
        WITH p, tr, t, duration.inSeconds(pt.startDate, pt.endDate).seconds AS durationSeconds
        WITH p, tr, count(DISTINCT t) AS taskCount, avg(durationSeconds) AS avgSeconds
        WHERE taskCount > 2 AND avgSeconds > 3600
        RETURN DISTINCT p
        """)
    List<Project> findProjectsWithHighTechnicalResourceWorkload(
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );

    @Query("""
        MATCH (p:Project)-[:HAS_TASK]->(t:Task)
        WHERE elementId(t) = $taskId
        WITH p, count(*) AS linkCount
        WHERE linkCount > 0
        RETURN elementId(p)
        """)
    Optional<String> findProjectIdByTaskId(String taskId);
}
