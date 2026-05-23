package org.example.projectrealizationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.OffsetDateTime;

@Node("ProblemReport")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReport {

    @Id
    @GeneratedValue
    private String id;

    private String reporterId;
    private String description;
    private ProblemType problemType;

    @Builder.Default
    private ProblemStatus status = ProblemStatus.OPEN;

    @Builder.Default
    private OffsetDateTime reportedAt = OffsetDateTime.now();
}
