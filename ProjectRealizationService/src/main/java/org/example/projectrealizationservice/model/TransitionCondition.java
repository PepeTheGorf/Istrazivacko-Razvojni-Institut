package org.example.projectrealizationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("TransitionCondition")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionCondition {

    @Id
    @GeneratedValue
    private String id;

    private String description;
    private TransitionType type;

    @Relationship(type = "FROM_PHASE", direction = Relationship.Direction.OUTGOING)
    private Phase fromPhase;

    @Relationship(type = "TO_PHASE", direction = Relationship.Direction.OUTGOING)
    private Phase toPhase;
}
