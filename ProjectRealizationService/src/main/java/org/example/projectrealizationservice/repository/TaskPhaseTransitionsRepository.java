package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TaskPhaseTransitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskPhaseTransitionsRepository extends JpaRepository<TaskPhaseTransitions, Long> {
    List<TaskPhaseTransitions> findAllByTaskAssignment_Task_Project(Project project);
    
    @Query("SELECT AVG(t.duration) FROM TaskPhaseTransitions t WHERE t.transitionCondition.fromPhase = :phase")
    Double averageDurationByPhase(@Param("phase") Phase phase);

    @Query("""
            SELECT AVG(t.duration) FROM TaskPhaseTransitions t
            JOIN t.transitionCondition tc
            JOIN tc.fromPhase fp
            JOIN t.taskAssignment ta
            JOIN ta.task task
            WHERE task.project = :project AND fp.name = :phaseName AND fp.order = :phaseOrder
            AND (:memberId IS NULL OR ta.assigneeId = :memberId)
            AND task.id = COALESCE(:taskId, task.id)
            AND t.transitionedAt >= COALESCE(:from, t.transitionedAt)
            AND t.transitionedAt <= COALESCE(:to, t.transitionedAt)
            """)
    Double averageDurationByPhaseNameAndOrder(
            @Param("project") Project project,
            @Param("phaseName") String phaseName,
            @Param("phaseOrder") int phaseOrder,
            @Param("memberId") Long memberId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT task.id AS taskId, COALESCE(SUM(t.duration), 0) AS totalDurationSeconds
            FROM TaskPhaseTransitions t
            JOIN t.taskAssignment ta
            JOIN ta.task task
            WHERE task.project = :project
            AND (:memberId IS NULL OR ta.assigneeId = :memberId)
            AND task.id = COALESCE(:taskId, task.id)
            AND t.transitionedAt >= COALESCE(:from, t.transitionedAt)
            AND t.transitionedAt <= COALESCE(:to, t.transitionedAt)
            GROUP BY task.id
            """)
    List<TaskDurationAggregate> sumDurationGroupedByTask(
            @Param("project") Project project,
            @Param("memberId") Long memberId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT fp.name AS fromPhaseName, tp.name AS toPhaseName,
                   t.duration AS durationSeconds, t.transitionedAt AS transitionedAt
            FROM TaskPhaseTransitions t
            JOIN t.transitionCondition tc
            JOIN tc.fromPhase fp
            JOIN tc.toPhase tp
            JOIN t.taskAssignment ta
            JOIN ta.task task
            WHERE task.id = :taskId
            AND t.transitionedAt >= COALESCE(:from, t.transitionedAt)
            AND t.transitionedAt <= COALESCE(:to, t.transitionedAt)
            ORDER BY t.transitionedAt ASC
            """)
    List<TaskPhaseHistoryAggregate> findTaskPhaseHistory(
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
