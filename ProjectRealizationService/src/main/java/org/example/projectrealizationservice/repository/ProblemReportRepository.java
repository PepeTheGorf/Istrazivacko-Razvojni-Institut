package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.ProblemReport;
import org.example.projectrealizationservice.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemReportRepository extends JpaRepository<ProblemReport, Long> {

    List<ProblemReport> findByTask_Id(Long taskId);

    @Query("SELECT COUNT(pr) FROM ProblemReport pr WHERE pr.task = :task AND pr.status IN ('IN_PROGRESS','OPEN')")
    Integer countUnresolvedProblems(Task task);
}
