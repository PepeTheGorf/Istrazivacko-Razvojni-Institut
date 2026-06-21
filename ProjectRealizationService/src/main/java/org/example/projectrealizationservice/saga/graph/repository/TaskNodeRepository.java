package org.example.projectrealizationservice.saga.graph.repository;

import org.example.projectrealizationservice.saga.graph.model.TaskNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TaskNodeRepository extends Neo4jRepository<TaskNode, String> {
}
