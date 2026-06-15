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
    private String promptTemplateId;       //partition key
    private String researcherId;           //clustering key1
    private UUID usageId;                  //clustering key2
    private Integer usageCount;
    private Float averageEffectiveness;
    private String lastUsedDate;
    private Boolean isActive;
}
