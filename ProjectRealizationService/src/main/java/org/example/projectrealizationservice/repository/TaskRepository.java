package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Task;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends Neo4jRepository<Task, String> {

    @Query("""
            MATCH (p:Project) WHERE elementId(p) = $projectId
            MATCH (t:Task) WHERE elementId(t) = $taskId
            MERGE (p)-[rel:HAS_TASK]->(t)
            ON CREATE SET rel.startDate = datetime(), rel.endDate = $endDate
            ON MATCH SET rel.endDate = $endDate
            """)
    void createOrUpdateTaskDates(String projectId, String taskId, OffsetDateTime endDate);

    @Query("""
            MATCH (p:Project) WHERE elementId(p) = $projectId
            MATCH (t:Task) WHERE elementId(t) = $taskId
            CREATE (p)-[rel:HAS_TASK]->(t)
            SET rel.startDate = datetime(), rel.endDate = $endDate
            """)
    void createProjectTask(String projectId, String taskId, OffsetDateTime endDate);

    @Query("""
            MATCH (p:Project) WHERE elementId(p) = $projectId
            MATCH (t:Task) WHERE elementId(t) = $taskId
            MERGE (p)-[pt:HAS_TASK]->(t)
            ON CREATE SET pt.startDate = coalesce($startDate, datetime()), pt.endDate = $endDate
            ON MATCH SET pt.endDate = $endDate
            """)
    void linkTaskToProject(String projectId, String taskId, OffsetDateTime startDate, OffsetDateTime endDate);


    @Query("""
            MATCH (p:Project)-[:HAS_TASK]->(t:Task)-[:IN_PHASE]->(ph:Phase)
            WHERE elementId(p) = $projectId
              AND elementId(ph) = $phaseId
            MATCH (t)-[:HAS_ACCEPTANCE_CRITERIA]->(ac:AcceptanceCriteria)
            WITH t, count(CASE WHEN ac.completed = true THEN 1 END) AS completedCount
            WHERE completedCount >= $minCompleted
            RETURN DISTINCT t
            """)
    List<Task> findTasksWithMinCompletedAcceptanceCriteriaByProject(
            String projectId,
            String phaseId,
            long minCompleted
    );

    @Query("""
            MATCH (t:Task) WHERE elementId(t) = $taskId
            DETACH DELETE t
            """)
    void detachDeleteById(String taskId);
}
