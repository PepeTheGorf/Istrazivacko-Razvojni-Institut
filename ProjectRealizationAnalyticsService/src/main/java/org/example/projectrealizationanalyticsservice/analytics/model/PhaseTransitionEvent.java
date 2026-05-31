package org.example.projectrealizationanalyticsservice.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhaseTransitionEvent {
    private String projectName;
    private String taskId;
    private String fromPhase;
    private String toPhase;
    private Long userId;
    private long durationInPreviousPhase;
    private Instant windowStart;
}
