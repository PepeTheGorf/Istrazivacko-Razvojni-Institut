package org.example.projectrealizationservice.dto.analytics;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskPhaseHistoryEntryDTO {
    private String fromPhaseName;
    private String toPhaseName;
    private long durationSeconds;
    private OffsetDateTime transitionedAt;
}
