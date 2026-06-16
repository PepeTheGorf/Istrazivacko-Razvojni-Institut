package org.example.projectrealizationservice.model.neo4j;

import lombok.*;
import org.example.projectrealizationservice.dto.creation.TransitionConditionDTO;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("TransitionConditionType")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionConditionType {
    @Id
    @GeneratedValue
    private String id;
    
    private String name;
    private String description;
}
