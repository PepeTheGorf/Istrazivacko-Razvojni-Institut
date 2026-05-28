package org.example.projectrealizationservice.repository.neo4j;

import org.example.projectrealizationservice.model.neo4j.Phase;
import org.example.projectrealizationservice.model.neo4j.Task;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends Neo4jRepository<Task, String> {

    List<Task> findByProjectId(Long projectId);

    @Query("""
        MATCH (subtask:Task)-[:SUBTASK_OF]->(parent:Task)
        WHERE parent.id = $parentTaskId
        RETURN subtask
        """)
    List<Task> findSubtasksByParentTaskId(String parentTaskId);

    @Query("""
        MATCH (t:Task)-[:IN_PHASE]->(p:Phase)
        WHERE p.name = $phaseName
        RETURN t
        """)
    List<Task> findTasksByPhaseName(String phaseName);

    @Query("""
        MATCH (t:Task)-[:IN_PHASE]->(p:Phase)
        WHERE p.name = $phaseName
        MATCH (tc:TransitionCondition)-[:FROM_PHASE]->(p)
        RETURN n
        """)
    List<Phase> findRecommendedPhaseTransitions(String taskId);
}
