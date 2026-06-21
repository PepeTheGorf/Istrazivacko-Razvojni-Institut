package org.example.projectrealizationservice.saga.smartdoc;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class SmartDocSagaInstance {

    private final String sagaId;
    private final SmartDocSagaContext context;
    private final OffsetDateTime createdAt;

    private SmartDocSagaState state;

    private UUID cassandraRecordId;
    private Long vectorRecordId;

    public SmartDocSagaInstance(String sagaId, SmartDocSagaContext context) {
        this.sagaId = sagaId;
        this.context = context;
        this.createdAt = OffsetDateTime.now();
        this.state = SmartDocSagaState.STARTED;
    }
}
