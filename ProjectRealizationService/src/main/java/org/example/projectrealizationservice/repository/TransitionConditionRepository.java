package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.TransitionCondition;
import org.example.projectrealizationservice.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransitionConditionRepository extends JpaRepository<TransitionCondition, Long> {
    List<TransitionCondition> findByFromPhaseAndToPhase(Phase fromPhase, Phase toPhase);

    List<TransitionCondition> findByWorkflow(Workflow workflow);
}
