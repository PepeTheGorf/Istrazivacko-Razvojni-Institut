package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.ProblemReport;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface ProblemReportRepository extends Neo4jRepository<ProblemReport, String> {
    
    @Query("""
            MATCH (t:Task)-[:HAS_PROBLEM]->(pr:ProblemReport)
            WHERE elementId(t) = $taskId
            RETURN pr
            """)
    List<ProblemReport> findAllByTaskId(String taskId);
}

