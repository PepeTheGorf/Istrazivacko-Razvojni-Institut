package org.example.projectrealizationservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Getter
@Setter
public class Phase {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int order;
}
