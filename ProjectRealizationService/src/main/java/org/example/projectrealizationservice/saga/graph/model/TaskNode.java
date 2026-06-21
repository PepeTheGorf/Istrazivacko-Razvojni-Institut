package org.example.projectrealizationservice.saga.graph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Task")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskNode {

    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("currentPhase")
    private String currentPhase;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private ProjectNode project;

    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.OUTGOING)
    private MemberNode assignee;

    @Relationship(type = "HAS_TRANSITION", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<PhaseTransitionNode> transitions = new ArrayList<>();
}
