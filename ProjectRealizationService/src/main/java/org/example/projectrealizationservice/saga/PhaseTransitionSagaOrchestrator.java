package org.example.projectrealizationservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projectrealizationservice.config.SagaRabbitMQConfig;
import org.example.projectrealizationservice.saga.graph.TaskGraphService;
import org.example.projectrealizationservice.saga.message.PhaseTransitionRecordedReply;
import org.example.projectrealizationservice.saga.message.RecordPhaseTransitionCommand;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PhaseTransitionSagaOrchestrator {

    private final TaskGraphService taskGraphService;
    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentHashMap<String, SagaInstance> registry = new ConcurrentHashMap<>();

    public String start(PhaseTransitionSagaContext context) {
        String sagaId = UUID.randomUUID().toString();
        SagaInstance instance = new SagaInstance(sagaId, context);
        registry.put(sagaId, instance);

        log.info("[SAGA] {} started for task={} ({} -> {})",
                sagaId, context.getTaskId(), context.getFromPhase(), context.getToPhase());

        try {
            String transitionId = taskGraphService.recordTransition(context);
            instance.setGraphTransitionId(transitionId);
            instance.setState(SagaState.GRAPH_WRITTEN);
            log.info("[SAGA] {} wrote transition {} into Neo4j", sagaId, transitionId);
        } catch (Exception e) {
            instance.setState(SagaState.FAILED);
            log.error("[SAGA] {} failed writing to Neo4j: {}", sagaId, e.getMessage(), e);
            return sagaId;
        }

        RecordPhaseTransitionCommand command = new RecordPhaseTransitionCommand(
                sagaId,
                context.getProjectName(),
                context.getTaskId(),
                context.getFromPhase(),
                context.getToPhase(),
                context.getAssigneeId(),
                context.getDurationSeconds());

        try {
            rabbitTemplate.convertAndSend(SagaRabbitMQConfig.EXCHANGE, SagaRabbitMQConfig.COMMAND_KEY, command);
            instance.setState(SagaState.ANALYTICS_REQUESTED);
            log.info("[SAGA] {} sent RecordPhaseTransitionCommand to analytics", sagaId);
        } catch (Exception e) {
            log.error("[SAGA] {} failed sending command, compensating: {}", sagaId, e.getMessage(), e);
            taskGraphService.removeTransition(instance.getGraphTransitionId());
            instance.setState(SagaState.COMPENSATED);
        }

        return sagaId;
    }

    @RabbitListener(queues = SagaRabbitMQConfig.REPLY_QUEUE)
    public void onReply(PhaseTransitionRecordedReply reply) {
        SagaInstance instance = registry.get(reply.getSagaId());
        if (instance == null) {
            log.warn("[SAGA] received reply for unknown saga {}", reply.getSagaId());
            return;
        }

        if (reply.isSuccess()) {
            instance.setState(SagaState.COMPLETED);
            log.info("[SAGA] {} completed, both stores updated", reply.getSagaId());
            return;
        }

        log.warn("[SAGA] {} analytics step failed ({}), compensating Neo4j", reply.getSagaId(), reply.getError());
        taskGraphService.removeTransition(instance.getGraphTransitionId());
        instance.setState(SagaState.COMPENSATED);
    }

    public SagaInstance getStatus(String sagaId) {
        return registry.get(sagaId);
    }
}
