package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = """
        SELECT all_subtasks_completed(:taskId)
    """, nativeQuery = true)
    Boolean allSubtasksCompleted(@Param("taskId") Long taskId);
    
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
}
