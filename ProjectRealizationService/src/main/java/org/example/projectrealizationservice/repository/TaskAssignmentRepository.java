package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    List<TaskAssignment> findByTask_Id(Long taskId);

    List<TaskAssignment> findByAssigneeId(Long assigneeId);

    Boolean existsByTaskAndAssigneeId(Task task, Long assigneeId);

    @Query("SELECT DISTINCT ta.assigneeId FROM TaskAssignment ta WHERE ta.task.project.id = :projectId")
    List<Long> findDistinctAssigneeIdsByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(COUNT(t), 0) FROM Task t INNER JOIN TaskAssignment ta ON ta.task = t " +
            "WHERE ta.assigneeId = :assigneeId AND t.project.id = :projectId")
    int countTaskAssignmentsByAssigneeIdAndProject(
            @Param("assigneeId") Long assigneeId,
            @Param("projectId") Long projectId
    );

    @Query("""
            SELECT COALESCE(COUNT(t), 0) FROM Task t
            INNER JOIN TaskAssignment ta ON ta.task = t
            JOIN t.phase p
            WHERE ta.assigneeId = :assigneeId AND t.project.id = :projectId
            AND NOT EXISTS (
                SELECT 1 FROM Phase later
                WHERE later.workflow = t.workflow AND later.order > p.order
            )
            """)
    int countCompletedTasksByAssigneeAndProject(
            @Param("assigneeId") Long assigneeId,
            @Param("projectId") Long projectId
    );

    @Query("SELECT COALESCE(COUNT(t), 0) FROM Task t INNER JOIN TaskAssignment ta ON ta.task = t " +
            "WHERE ta.assigneeId = :assigneeId AND t.project.id = :projectId AND t.endDate < CURRENT_TIMESTAMP")
    int countOverdueTasksByAssigneeAndProject(
            @Param("assigneeId") Long assigneeId,
            @Param("projectId") Long projectId
    );
}
