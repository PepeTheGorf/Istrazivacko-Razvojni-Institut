package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.dto.analytics.DelayedTaskSummary;
import org.example.projectrealizationservice.dto.analytics.PhaseTaskDurationSummary;
import org.example.projectrealizationservice.dto.analytics.TechnicalResourceWorkloadSummary;
import org.example.projectrealizationservice.model.Project;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, Long> {
    Optional<Project> findByName(String name);

    @Query("MATCH (p:Project {id: $projectId})-[pt:HAS_TASK]->(t:Task) " +
            "WHERE pt.startDate >= $startDate AND pt.endDate <= $endDate " +
            "WITH " +
            "t.phase.name AS phase, " +
                "avg(duration.inSeconds(pt.startDate, pt.endDate)) AS averageDurationSeconds, " +
                "count(t) AS taskCount " +
            "RETURN phase, averageDurationSeconds, taskCount"
    )
    List<PhaseTaskDurationSummary> analyzeTaskDurationByPhase(
            Long projectId,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );

    @Query("MATCH (p:Project {id: $projectId})-[pt:HAS_TASK]->(t:Task) " +
            "WHERE pt.endDate < $currentDate " +
            "MATCH (t)-[:USES_WORKFLOW]->(w:Workflow)-[:HAS_PHASE]->(phases) " +
            "WITH t, pt, w, max(phases.order) AS maxPhaseOrder " +
            "WHERE t.phase.order <> maxPhaseOrder " +
            "RETURN t.name AS taskName, " +
            "t.phase.name AS currentPhase, " +
            "duration.inSeconds(pt.startDate, pt.endDate) AS durationSeconds"
    )
    List<DelayedTaskSummary> findDelayedTasks(Long projectId, OffsetDateTime currentDate);

    @Query("MATCH (p: Project {id: $projectId})-[pt:HAS_TASK]->(t:Task)-[:HAS_TECHNICAL_RESOURCE]->(tr:TechnicalResource) " +
            "WHERE pt.startDate >= $startDate AND pt.endDate <= $endDate " +
            "WITH tr.name AS resourceName, " +
                "count(t) AS taskCount, " +
                "avg(duration.inSeconds(pt.startDate, pt.endDate)) AS averageDurationSeconds " +
            "WHERE taskCount > 3 AND averageDurationSeconds > 864000 " +
            "RETURN resourceName, taskCount, averageDurationSeconds"
    )
    List<TechnicalResourceWorkloadSummary> analyzeTechnicalResourceWorkload(
            Long projectId,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );
}
