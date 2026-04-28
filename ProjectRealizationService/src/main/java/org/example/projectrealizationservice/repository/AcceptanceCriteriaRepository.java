package org.example.projectrealizationservice.repository;

import org.example.projectrealizationservice.model.AcceptanceCriteria;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcceptanceCriteriaRepository extends Neo4jRepository<AcceptanceCriteria, String> {
    
}
