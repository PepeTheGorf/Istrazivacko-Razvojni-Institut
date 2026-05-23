package org.example.projectrealizationservice.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAssignment {
    @RelationshipId
    @GeneratedValue
    private String id;
    private Integer quantity;
    
    @TargetNode
    private TechnicalResource technicalResource;
}
