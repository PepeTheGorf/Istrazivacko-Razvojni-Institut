package org.example.projectrealizationservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
@Getter
@Setter
public class ProjectTask {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @TargetNode
    private Task task;
}
