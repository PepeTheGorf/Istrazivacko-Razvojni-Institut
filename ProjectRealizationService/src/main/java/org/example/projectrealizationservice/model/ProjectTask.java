package org.example.projectrealizationservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.OffsetDateTime;

@RelationshipProperties
@Getter
@Setter
@Builder
public class ProjectTask {
    @RelationshipId
    @GeneratedValue
    private String id;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    @TargetNode
    private Task task;
}
