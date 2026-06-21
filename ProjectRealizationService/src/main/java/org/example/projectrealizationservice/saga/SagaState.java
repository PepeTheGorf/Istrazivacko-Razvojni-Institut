package org.example.projectrealizationservice.saga;

public enum SagaState {
    STARTED,
    GRAPH_WRITTEN,
    ANALYTICS_REQUESTED,
    COMPLETED,
    COMPENSATED,
    FAILED
}
