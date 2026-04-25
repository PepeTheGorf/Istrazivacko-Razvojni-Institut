package org.example.projectrealizationservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Getter
@Setter
@Node
public class TransitionCondition {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private TransitionType type;
}
