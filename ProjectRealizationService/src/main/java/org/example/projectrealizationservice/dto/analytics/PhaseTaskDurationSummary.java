package org.example.projectrealizationservice.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhaseTaskDurationSummary {
    private String phase;
    private Double averageDurationSeconds;
    private Long taskCount;
}
