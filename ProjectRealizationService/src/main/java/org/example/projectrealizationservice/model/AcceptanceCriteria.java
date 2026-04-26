package org.example.projectrealizationservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Getter
@Setter
@Node
public class AcceptanceCriteria {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private boolean completed;
}
