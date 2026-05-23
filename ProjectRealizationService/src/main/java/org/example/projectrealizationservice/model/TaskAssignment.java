package org.example.projectrealizationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.time.OffsetDateTime;

@Node("TaskAssignment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignment {
    @Id
    @GeneratedValue
    private String id;

    private Long assigneeId;
    private OffsetDateTime assignedAt;
    
    //todo: add role of assignee in task assignment
}
