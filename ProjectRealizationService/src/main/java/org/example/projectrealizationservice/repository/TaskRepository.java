package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    @Query("""
            SELECT t FROM Task t
            LEFT JOIN FETCH t.phase
            LEFT JOIN FETCH t.workflow w
            LEFT JOIN FETCH w.phases
            WHERE t.project.id = :projectId AND t.parentTask IS NULL
            """)
    List<Task> findRootTasksByProjectId(@Param("projectId") Long projectId);

    @Query("""
            SELECT t FROM Task t
            LEFT JOIN FETCH t.phase
            LEFT JOIN FETCH t.workflow w
            LEFT JOIN FETCH w.phases
            WHERE t.parentTask.id = :parentTaskId
            """)
    List<Task> findSubtasksByParentTaskId(@Param("parentTaskId") Long parentTaskId);

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN FETCH t.phase
            LEFT JOIN FETCH t.workflow w
            LEFT JOIN FETCH w.phases
            LEFT JOIN FETCH t.project
            LEFT JOIN FETCH t.subTasks st
            LEFT JOIN FETCH st.phase
            LEFT JOIN FETCH st.workflow sw
            LEFT JOIN FETCH sw.phases
            WHERE t.id = :taskId
            """)
    Optional<Task> findByIdWithDetails(@Param("taskId") Long taskId);
    
    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.phase
        LEFT JOIN FETCH t.workflow
        JOIN TaskAssignment ta ON ta.task = t AND ta.assigneeId = :userId
        WHERE t.project.id = :projectId
        """)
    List<Task> findMyTasksByProject(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT t.project FROM Task t
        JOIN TaskAssignment ta ON ta.task = t AND ta.assigneeId = :userId
        ORDER BY t.project.name
        """)
    List<org.example.projectrealizationservice.model.Project> findProjectsForAssignee(@Param("userId") Long userId);

    boolean existsByPhase_Id(Long phaseId);

    boolean existsByWorkflow_Id(Long workflowId);

    @Query(value = """
            SELECT add_task_phase_transition(
                :taskId,
                :oldPhaseId,
                :newPhaseId,
                :workflowId,
                :assigneeId,
                CAST(:transitionedAt AS TIMESTAMPTZ),
                :duration
            )
            """, nativeQuery = true)
    void addTaskPhaseTransition(
            @Param("taskId") Long taskId,
            @Param("oldPhaseId") Long oldPhaseId,
            @Param("newPhaseId") Long newPhaseId,
            @Param("workflowId") Long workflowId,
            @Param("assigneeId") Long assigneeId,
            @Param("transitionedAt") OffsetDateTime transitionedAt,
            @Param("duration") Long duration
    );

    Integer countTasksByPhaseAndProject(Phase phase, Project project);

    @Query("""
            SELECT p.id AS phaseId, p.name AS phaseName, p.order AS phaseOrder, COUNT(t) AS taskCount
            FROM Task t
            JOIN t.phase p
            WHERE t.project = :project
            AND (:memberId IS NULL OR EXISTS (
                SELECT 1 FROM TaskAssignment ta WHERE ta.task = t AND ta.assigneeId = :memberId
            ))
            AND t.id = COALESCE(:taskId, t.id)
            AND t.endDate >= COALESCE(:from, t.endDate)
            AND t.startDate <= COALESCE(:to, t.startDate)
            GROUP BY p.id, p.name, p.order
            ORDER BY p.order ASC, p.name ASC
            """)
    List<PhaseTaskCountAggregate> aggregatePhaseTaskCountsByProject(
            @Param("project") Project project,
            @Param("memberId") Long memberId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.project = :project
            AND (:memberId IS NULL OR EXISTS (
                SELECT 1 FROM TaskAssignment ta WHERE ta.task = t AND ta.assigneeId = :memberId
            ))
            AND t.id = COALESCE(:taskId, t.id)
            AND t.endDate >= COALESCE(:from, t.endDate)
            AND t.startDate <= COALESCE(:to, t.startDate)
            """)
    Integer countTasksByProject(
            @Param("project") Project project,
            @Param("memberId") Long memberId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
    
    @Query("""
            SELECT COUNT(t) FROM Task t
            JOIN t.phase p
            WHERE t.project = :project
            AND NOT EXISTS (
                SELECT 1 FROM Phase later
                WHERE later.workflow = t.workflow AND later.order > p.order
            )
            AND (:memberId IS NULL OR EXISTS (
                SELECT 1 FROM TaskAssignment ta WHERE ta.task = t AND ta.assigneeId = :memberId
            ))
            AND t.id = COALESCE(:taskId, t.id)
            AND t.endDate >= COALESCE(:from, t.endDate)
            AND t.startDate <= COALESCE(:to, t.startDate)
            """)
    Integer countCompletedTasksByProject(
            @Param("project") Project project,
            @Param("memberId") Long memberId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT COUNT(t) FROM Task t
            JOIN t.phase p
            WHERE t.project = :project
            AND t.endDate < CURRENT_TIMESTAMP
            AND EXISTS (
                SELECT 1 FROM Phase later
                WHERE later.workflow = t.workflow AND later.order > p.order
            )
            AND (:memberId IS NULL OR EXISTS (
                SELECT 1 FROM TaskAssignment ta WHERE ta.task = t AND ta.assigneeId = :memberId
            ))
            AND t.id = COALESCE(:taskId, t.id)
            AND t.endDate >= COALESCE(:from, t.endDate)
            AND t.startDate <= COALESCE(:to, t.startDate)
            """)
    Integer countOverdueTasksByProject(
            @Param("project") Project project,
            @Param("memberId") Long memberId,
            @Param("taskId") Long taskId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
