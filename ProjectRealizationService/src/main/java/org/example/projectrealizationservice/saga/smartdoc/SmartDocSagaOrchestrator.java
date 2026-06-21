package org.example.projectrealizationservice.saga.smartdoc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projectrealizationservice.client.CassandraClient;
import org.example.projectrealizationservice.client.VectorDatabaseClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmartDocSagaOrchestrator {

    private final CassandraClient cassandraClient;
    private final VectorDatabaseClient vectorDatabaseClient;

    private final ConcurrentHashMap<String, SmartDocSagaInstance> registry = new ConcurrentHashMap<>();

    public SmartDocSagaInstance startGenerateSaga(SmartDocSagaContext context) {
        String sagaId = UUID.randomUUID().toString();
        SmartDocSagaInstance instance = new SmartDocSagaInstance(sagaId, context);
        registry.put(sagaId, instance);

        log.info("[GENERATE-SAGA] {} started — researcher={}, section={}",
                sagaId, context.getResearcherId(), context.getSectionTitle());

        UUID cassandraId;
        try {
            cassandraId = cassandraClient.insertLlmRequest(context.getResearcherId(), "SUCCESS");
            instance.setCassandraRecordId(cassandraId);
            instance.setState(SmartDocSagaState.CASSANDRA_WRITTEN);
            log.info("[GENERATE-SAGA] {} -> CASSANDRA_WRITTEN, recordId={}", sagaId, cassandraId);
        } catch (Exception e) {
            instance.setState(SmartDocSagaState.FAILED);
            log.error("[GENERATE-SAGA] {} -> FAILED na Cassandra upisu: {}", sagaId, e.getMessage());
            return instance;
        }

        try {
            Long vectorId = vectorDatabaseClient.createDocument(
                    context.getTemplateName() + " — " + context.getSectionTitle(),
                    context.getResearcherId(),
                    context.getGeneratedContent(),
                    List.of(context.getDomainName(), context.getCategoryName()),
                    Map.of("section", context.getSectionTitle(), "template", context.getTemplateName())
            );
            instance.setVectorRecordId(vectorId);
            instance.setState(SmartDocSagaState.VECTOR_WRITTEN);
            instance.setState(SmartDocSagaState.COMPLETED);
            log.info("[GENERATE-SAGA] {} -> COMPLETED, vectorDocumentId={}", sagaId, vectorId);
        } catch (Exception e) {
            log.error("[GENERATE-SAGA] {} -> Vector upis FAILED: {}, pokrećem kompenzaciju", sagaId, e.getMessage());
            compensateGenerate(instance);
        }

        return instance;
    }

    private void compensateGenerate(SmartDocSagaInstance instance) {
        instance.setState(SmartDocSagaState.COMPENSATING);
        try {
            cassandraClient.deleteLlmRequest(instance.getContext().getResearcherId(), instance.getCassandraRecordId());
            instance.setState(SmartDocSagaState.COMPENSATED);
            log.warn("[GENERATE-SAGA] {} -> COMPENSATED — Cassandra upis poništen", instance.getSagaId());
        } catch (Exception e) {
            instance.setState(SmartDocSagaState.FAILED);
            log.error("[GENERATE-SAGA] {} -> FAILED — kompenzacija nije uspela: {}", instance.getSagaId(), e.getMessage());
        }
    }

    public SmartDocSagaInstance startFeedbackSaga(SmartDocSagaContext context) {
        String sagaId = UUID.randomUUID().toString();
        SmartDocSagaInstance instance = new SmartDocSagaInstance(sagaId, context);
        registry.put(sagaId, instance);

        log.info("[FEEDBACK-SAGA] {} started — domain={}, rating={}", sagaId, context.getDomainName(), context.getRating());

        UUID cassandraId;
        try {
            cassandraId = cassandraClient.insertFeedback(context.getDomainName(), context.getRating());
            instance.setCassandraRecordId(cassandraId);
            instance.setState(SmartDocSagaState.CASSANDRA_WRITTEN);
            log.info("[FEEDBACK-SAGA] {} -> CASSANDRA_WRITTEN, recordId={}", sagaId, cassandraId);
        } catch (Exception e) {
            instance.setState(SmartDocSagaState.FAILED);
            log.error("[FEEDBACK-SAGA] {} -> FAILED na Cassandra upisu: {}", sagaId, e.getMessage());
            return instance;
        }

        try {
            Long vectorId = vectorDatabaseClient.createDocument(
                    "Feedback — " + context.getSectionTitle(),
                    context.getResearcherId(),
                    context.getFeedbackComment(),
                    List.of(context.getDomainName(), context.getCategoryName(), "feedback"),
                    Map.of(
                        "rating", String.valueOf(context.getRating()),
                        "section", context.getSectionTitle(),
                        "template", context.getTemplateName()
                    )
            );
            instance.setVectorRecordId(vectorId);
            instance.setState(SmartDocSagaState.VECTOR_WRITTEN);
            instance.setState(SmartDocSagaState.COMPLETED);
            log.info("[FEEDBACK-SAGA] {} -> COMPLETED, vectorChunkId={}", sagaId, vectorId);
        } catch (Exception e) {
            log.error("[FEEDBACK-SAGA] {} -> Vector upis FAILED: {}, pokrećem kompenzaciju", sagaId, e.getMessage());
            compensateFeedback(instance);
        }

        return instance;
    }

    private void compensateFeedback(SmartDocSagaInstance instance) {
        instance.setState(SmartDocSagaState.COMPENSATING);
        try {
            cassandraClient.deleteFeedback(instance.getContext().getDomainName(), instance.getCassandraRecordId());
            instance.setState(SmartDocSagaState.COMPENSATED);
            log.warn("[FEEDBACK-SAGA] {} -> COMPENSATED — Cassandra feedback poništen", instance.getSagaId());
        } catch (Exception e) {
            instance.setState(SmartDocSagaState.FAILED);
            log.error("[FEEDBACK-SAGA] {} -> FAILED — kompenzacija nije uspela: {}", instance.getSagaId(), e.getMessage());
        }
    }

    private void compensateVector(SmartDocSagaInstance instance, String sagaType) {
        if (instance.getVectorRecordId() != null) {
            try {
                vectorDatabaseClient.deleteDocument(instance.getVectorRecordId());
                log.warn("[{}-SAGA] {} -> vector dokument {} obrisan (kompenzacija)", sagaType, instance.getSagaId(), instance.getVectorRecordId());
            } catch (Exception e) {
                log.error("[{}-SAGA] {} -> brisanje vector dokumenta nije uspelo: {}", sagaType, instance.getSagaId(), e.getMessage());
            }
        }
    }

    public SmartDocSagaInstance getStatus(String sagaId) {
        return registry.get(sagaId);
    }
}
