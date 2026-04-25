package org.example.projectrealizationservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Node
@Getter
@Setter
public class Task {
    @Id
    private Long id;
    private String name;
    private String description;

    @Relationship(type = "SUBTASK_OF", direction = Relationship.Direction.OUTGOING)
    private Task parentTask;
    
    @Relationship(type = "USES_WORKFLOW", direction = Relationship.Direction.OUTGOING)
    private Workflow workflow;
    
    @Relationship(type = "IN_PHASE", direction = Relationship.Direction.OUTGOING)
    private Phase phase;
    
    @Relationship(type = "HAS_ACCEPTANCE_CRITERIA", direction = Relationship.Direction.OUTGOING)
    private Set<AcceptanceCriteria> acceptanceCriteria = new HashSet<>();
    
    @Relationship(type = "HAS_TECHNICAL_RESOURCE", direction = Relationship.Direction.OUTGOING)
    private Set<TechnicalResource> technicalResources = new HashSet<>();
}
