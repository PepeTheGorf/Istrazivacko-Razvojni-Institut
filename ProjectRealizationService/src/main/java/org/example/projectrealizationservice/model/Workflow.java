package org.example.projectrealizationservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node
@Getter
@Setter
@Builder
public class Workflow {
    @Id
    @GeneratedValue
    private String id;
    private String name;
    private String description;

    @Relationship(type = "HAS_PHASE", direction = Relationship.Direction.OUTGOING)
    private Set<Phase> phases;
}
