package org.example.projectrealizationservice.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PhaseTransitionSagaContext {

    private final String taskId;
    private final String taskName;
    private final String projectName;
    private final Long assigneeId;
    private final String fromPhase;
    private final String toPhase;
    private final long durationSeconds;
}
