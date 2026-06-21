package org.example.projectrealizationservice.saga;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class SagaInstance {

    private final String sagaId;
    private final PhaseTransitionSagaContext context;
    private final OffsetDateTime createdAt;

    private SagaState state;
    private String graphTransitionId;

    public SagaInstance(String sagaId, PhaseTransitionSagaContext context) {
        this.sagaId = sagaId;
        this.context = context;
        this.createdAt = OffsetDateTime.now();
        this.state = SagaState.STARTED;
    }
}
