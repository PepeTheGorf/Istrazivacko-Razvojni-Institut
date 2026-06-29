package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    List<TaskAssignment> findByTask_Id(Long taskId);

    List<TaskAssignment> findByAssigneeId(Long assigneeId);

    Boolean existsByTaskAndAssigneeId(Task task, Long assigneeId);

    @Query("SELECT DISTINCT ta.assigneeId FROM TaskAssignment ta WHERE ta.task.project.id = :projectId")
    List<Long> findDistinctAssigneeIdsByProjectId(@Param("projectId") Long projectId);

    @Query("""
            SELECT COALESCE(COUNT(t), 0) FROM Task t
            INNER JOIN TaskAssignment ta ON ta.task = t
            WHERE ta.assigneeId = :assigneeId AND t.project.id = :projectId
            AND t.id = COALESCE(:taskId, t.id)
            AND t.endDate >= COALESCE(:from, t.endDate)
            AND t.startDate <= COALESCE(:to, t.startDate)
            """)
    int countTaskAssignmentsByAssigneeIdAndProject(
            @Param("assigneeId") Long assigneeId,
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT COALESCE(COUNT(t), 0) FROM Task t
            INNER JOIN TaskAssignment ta ON ta.task = t
            JOIN t.phase p
            WHERE ta.assigneeId = :assigneeId AND t.project.id = :projectId
            AND t.phase.order = (SELECT MAX(p2.order) FROM Phase p2 WHERE p2.workflow = t.workflow)
            AND t.id = COALESCE(:taskId, t.id)
            AND t.endDate >= COALESCE(:from, t.endDate)
            AND t.startDate <= COALESCE(:to, t.startDate)
            """)
    int countCompletedTasksByAssigneeAndProject(
            @Param("assigneeId") Long assigneeId,
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT COALESCE(COUNT(t), 0) FROM Task t
            INNER JOIN TaskAssignment ta ON ta.task = t
            JOIN t.phase p
            WHERE ta.assigneeId = :assigneeId AND t.project.id = :projectId
            AND t.endDate < CURRENT_TIMESTAMP
            AND EXISTS (
                SELECT 1 FROM Phase p2 WHERE p2.workflow = t.workflow AND p2.order > p.order
            )
            AND t.id = COALESCE(:taskId, t.id)
            AND t.endDate >= COALESCE(:from, t.endDate)
            AND t.startDate <= COALESCE(:to, t.startDate)
            """)
    int countOverdueTasksByAssigneeAndProject(
            @Param("assigneeId") Long assigneeId,
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
