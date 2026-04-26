package org.example.projectrealizationservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.time.OffsetDateTime;

@RelationshipProperties
@Getter
@Setter
public class ProjectTask {
    @RelationshipId
    @GeneratedValue
    private Long id;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    @TargetNode
    private Task task;
}
