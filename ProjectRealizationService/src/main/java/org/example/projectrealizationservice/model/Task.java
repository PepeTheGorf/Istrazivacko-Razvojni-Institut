package org.example.projectrealizationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Node("Task")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue
    private String id;

    private String name;
    private String description;

    private Long creatorId;
    
    private OffsetDateTime phaseChangeDate;
    
    @Relationship(type = "SUBTASK_OF", direction = Relationship.Direction.OUTGOING)
    private Task parentTask;

    @Relationship(type = "USES_WORKFLOW", direction = Relationship.Direction.OUTGOING)
    private Workflow workflow;

    @Relationship(type = "IN_PHASE", direction = Relationship.Direction.OUTGOING)
    private Phase phase;

    @Relationship(type = "HAS_ACCEPTANCE_CRITERIA", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<AcceptanceCriteria> acceptanceCriteria = new HashSet<>();

    @Relationship(type = "HAS_TECHNICAL_RESOURCE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<ResourceAssignment> technicalResources = new HashSet<>();

    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<TaskAssignment> assignments = new HashSet<>();

    @Relationship(type = "HAS_PROBLEM", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<ProblemReport> problems = new HashSet<>();
}
