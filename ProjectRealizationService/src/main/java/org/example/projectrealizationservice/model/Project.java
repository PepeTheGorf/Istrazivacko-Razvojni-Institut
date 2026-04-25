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
public class Project {
    @Id
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Relationship(value = "HAS_TASK", direction = Relationship.Direction.OUTGOING)
    private Set<ProjectTask> tasks = new HashSet<>();
}
