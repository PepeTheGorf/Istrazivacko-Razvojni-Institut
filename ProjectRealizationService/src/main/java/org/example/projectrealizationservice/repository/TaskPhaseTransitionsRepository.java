package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TaskPhaseTransitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            """)
    Double averageDurationByPhaseNameAndOrder(
            @Param("project") Project project,
            @Param("phaseName") String phaseName,
            @Param("phaseOrder") int phaseOrder
    );
}
