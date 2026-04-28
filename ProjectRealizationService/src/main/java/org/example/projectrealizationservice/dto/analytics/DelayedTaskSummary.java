package org.example.projectrealizationservice.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DelayedTaskSummary {
    private String taskName;
    private String currentPhase;
    private Long durationSeconds;
}
