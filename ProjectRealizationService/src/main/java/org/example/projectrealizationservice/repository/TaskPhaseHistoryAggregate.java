package org.example.projectrealizationservice.repository;

import java.time.OffsetDateTime;

public interface TaskPhaseHistoryAggregate {
    String getFromPhaseName();

    String getToPhaseName();

    Long getDurationSeconds();

    OffsetDateTime getTransitionedAt();
}
