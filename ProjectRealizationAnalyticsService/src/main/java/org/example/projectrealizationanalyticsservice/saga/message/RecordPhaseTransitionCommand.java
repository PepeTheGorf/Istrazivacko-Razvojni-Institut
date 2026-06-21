package org.example.projectrealizationanalyticsservice.saga.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordPhaseTransitionCommand {

    private String sagaId;
    private String projectName;
    private String taskId;
    private String fromPhase;
    private String toPhase;
    private Long userId;
    private long durationSeconds;
}
