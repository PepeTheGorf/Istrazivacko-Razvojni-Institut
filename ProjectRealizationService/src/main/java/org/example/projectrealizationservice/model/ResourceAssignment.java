package org.example.projectrealizationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    private Long creatorId;

    @TargetNode
    private TechnicalResource technicalResource;
}
