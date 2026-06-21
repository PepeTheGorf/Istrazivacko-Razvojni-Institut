package org.example.projectrealizationservice.saga.graph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("PhaseTransition")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseTransitionNode {

    @Id
    private String id;

    @Property("fromPhase")
    private String fromPhase;

    @Property("toPhase")
    private String toPhase;

    @Property("durationSeconds")
    private long durationSeconds;

    @Property("performedBy")
    private Long performedBy;

    @Property("recordedAt")
    private String recordedAt;
}
