package org.example.projectrealizationservice.saga.smartdoc;

public enum SmartDocSagaState {
    STARTED,
    CASSANDRA_WRITTEN,
    VECTOR_WRITTEN,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
