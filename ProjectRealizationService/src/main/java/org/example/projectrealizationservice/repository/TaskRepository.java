package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.dto.analytics.AcceptanceCriteriaCompletionSummary;
import org.example.projectrealizationservice.model.Task;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends Neo4jRepository<Task, Long> {

    @Query("MATCH (p:Project {id: $projectId}) " +
            "MATCH (t:Task {id: $taskId}) " +
            "MERGE (p)-[rel:HAS_TASK]->(t) " +
            "ON CREATE SET rel.startDate = datetime(), rel.endDate = $endDate " +
            "ON MATCH SET rel.endDate = $endDate")
    void createOrUpdateTaskDates(Long projectId, Long taskId, OffsetDateTime endDate);

    @Query("MATCH (t:Task {id: $taskId})-[:HAS_ACCEPTANCE_CRITERIA]->(ac:AcceptanceCriteria) " +
            "MATCH (t)-[:IN_PHASE]->(ph:Phase) " +
            "WHERE ph.id = $phaseId " +
            "WITH t.name AS taskName, " +
            "count(ac) AS totalCriteria, " +
            "count(CASE WHEN ac.completed = true THEN 1 END) AS completedCriteria " +
            "RETURN taskName, totalCriteria, completedCriteria"
    )
    List<AcceptanceCriteriaCompletionSummary> analyzeAcceptanceCriteriaCompletion(Long taskId, Long phaseId);
}
