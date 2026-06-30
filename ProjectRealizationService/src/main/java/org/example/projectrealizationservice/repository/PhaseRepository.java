package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, Long> {

    Optional<Phase> findFirstByWorkflow_IdOrderByOrderDesc(Long workflowId);
}
