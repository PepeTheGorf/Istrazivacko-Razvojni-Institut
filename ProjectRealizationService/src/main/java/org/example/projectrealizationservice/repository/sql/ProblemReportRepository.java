package org.example.projectrealizationservice.repository.sql;

import org.example.projectrealizationservice.model.sql.ProblemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemReportRepository extends JpaRepository<ProblemReport, Long> {

    List<ProblemReport> findByTaskId(String taskId);
}
