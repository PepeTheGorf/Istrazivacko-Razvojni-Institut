package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.sql.AcceptanceCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcceptanceCriteriaRepository extends JpaRepository<AcceptanceCriteria, Long> {

    List<AcceptanceCriteria> findByTaskId(String taskId);
}
