package org.example.projectrealizationanalyticsservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projectrealizationanalyticsservice.analytics.model.PhaseTransitionEvent;
import org.example.projectrealizationanalyticsservice.config.SagaRabbitMQConfig;
import org.example.projectrealizationanalyticsservice.saga.message.PhaseTransitionRecordedReply;
import org.example.projectrealizationanalyticsservice.saga.message.RecordPhaseTransitionCommand;
import org.example.projectrealizationanalyticsservice.service.PhaseTransitionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhaseTransitionCommandListener {

    private final PhaseTransitionService phaseTransitionService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = SagaRabbitMQConfig.COMMAND_QUEUE)
    public void onCommand(RecordPhaseTransitionCommand command) {
        log.info("[SAGA] received RecordPhaseTransitionCommand for saga {}", command.getSagaId());

        PhaseTransitionRecordedReply reply = new PhaseTransitionRecordedReply();
        reply.setSagaId(command.getSagaId());

        try {
            PhaseTransitionEvent event = new PhaseTransitionEvent();
            event.setProjectName(command.getProjectName());
            event.setTaskId(command.getTaskId());
            event.setFromPhase(command.getFromPhase());
            event.setToPhase(command.getToPhase());
            event.setUserId(command.getUserId());
            event.setDurationInPreviousPhase(command.getDurationSeconds());

            phaseTransitionService.transferTaskPhase(event);

            reply.setSuccess(true);
            log.info("[SAGA] saga {} written to InfluxDB", command.getSagaId());
        } catch (Exception e) {
            reply.setSuccess(false);
            reply.setError(e.getMessage());
            log.error("[SAGA] saga {} failed writing to InfluxDB: {}", command.getSagaId(), e.getMessage(), e);
        }

        rabbitTemplate.convertAndSend(SagaRabbitMQConfig.EXCHANGE, SagaRabbitMQConfig.REPLY_KEY, reply);
    }
}
