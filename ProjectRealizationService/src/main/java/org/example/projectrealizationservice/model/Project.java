package org.example.projectrealizationservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Node
@Getter
@Setter
@Builder
public class Project {
    @Id
    @GeneratedValue
    private String id;
    private String name;
    private String description;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    @Relationship(value = "HAS_TASK", direction = Relationship.Direction.OUTGOING)
    private Set<ProjectTask> tasks;
}
