package org.example.projectrealizationservice.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalResourceWorkloadSummary {
    private String resourceName;
    private Long taskCount;
    private Double averageDurationSeconds;
}
