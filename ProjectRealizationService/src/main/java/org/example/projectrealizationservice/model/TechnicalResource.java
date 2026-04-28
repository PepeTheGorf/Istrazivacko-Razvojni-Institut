package org.example.projectrealizationservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Setter
@Getter
@Node
@Builder
public class TechnicalResource {
    @Id
    @GeneratedValue
    private String id;
    private String name;
    private String description;
}
