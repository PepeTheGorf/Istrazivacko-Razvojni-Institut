package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.AcceptanceCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcceptanceCriteriaRepository extends JpaRepository<AcceptanceCriteria, Long> {

    List<AcceptanceCriteria> findByTask_Id(Long taskId);

    @Query("""
        SELECT CASE
            WHEN COUNT(ac) = SUM(CASE WHEN ac.completed = true THEN 1 ELSE 0 END)
            THEN true
            ELSE false
        END
        FROM AcceptanceCriteria ac
        WHERE ac.task.id = :taskId
    """)
    Boolean allAcceptanceCriteriaMetByTaskId(@Param("taskId") Long taskId);
}
