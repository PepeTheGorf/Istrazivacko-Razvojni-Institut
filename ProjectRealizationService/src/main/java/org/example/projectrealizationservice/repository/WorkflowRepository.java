package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    @Query("SELECT DISTINCT w FROM Workflow w LEFT JOIN FETCH w.phases WHERE w.name = :name")
    Optional<Workflow> findByName(@Param("name") String name);

    @Query("SELECT DISTINCT w FROM Workflow w LEFT JOIN FETCH w.phases")
    List<Workflow> findAllWithPhases();

    @Query("SELECT DISTINCT w FROM Workflow w LEFT JOIN FETCH w.phases WHERE w.id = :id")
    Optional<Workflow> findByIdWithPhases(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT w FROM Workflow w
            LEFT JOIN FETCH w.phases
            LEFT JOIN FETCH w.transitionConditions tc
            LEFT JOIN FETCH tc.transitionType
            LEFT JOIN FETCH tc.fromPhase
            LEFT JOIN FETCH tc.toPhase
            WHERE w.id = :id
            """)
    Optional<Workflow> findByIdWithDetails(@Param("id") Long id);
}
