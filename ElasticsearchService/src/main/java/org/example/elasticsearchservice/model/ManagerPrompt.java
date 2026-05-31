package org.example.elasticsearchservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagerPrompt {
    private String id;
    private String promptName;
    private String promptTemplate;
    private String category;
    private Integer usageCount;
    private Float effectivenessScore;
    private String department;
    private Long createdAt;
    private String tags;
}
