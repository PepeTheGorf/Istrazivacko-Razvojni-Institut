package org.example.cassandraservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptUsageByTemplate {
    private String promptTemplateId;       // Partition Key
    private String researcherId;           // Clustering Key 1
    private UUID usageId;                  // Clustering Key 2
    private Integer usageCount;
    private Float averageEffectiveness;
    private String lastUsedDate;
    private Boolean isActive;
}
