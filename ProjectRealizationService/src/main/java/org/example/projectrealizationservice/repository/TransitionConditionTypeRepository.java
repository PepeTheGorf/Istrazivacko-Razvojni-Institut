package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.TransitionConditionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransitionConditionTypeRepository extends JpaRepository<TransitionConditionType, Long> {

    Optional<TransitionConditionType> findByName(String name);

    boolean existsByName(String name);
}
