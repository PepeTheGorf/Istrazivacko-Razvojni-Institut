package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.saga.PhaseTransitionSagaOrchestrator;
import org.example.projectrealizationservice.saga.SagaInstance;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/tasks/saga")
@Profile("!test")
@RequiredArgsConstructor
public class SagaController {

    private final PhaseTransitionSagaOrchestrator sagaOrchestrator;

    @GetMapping("/{sagaId}")
    public ResponseEntity<?> getSagaStatus(@PathVariable String sagaId) {
        SagaInstance instance = sagaOrchestrator.getStatus(sagaId);
        if (instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Saga with id " + sagaId + " not found"));
        }

        return ResponseEntity.ok(Map.of(
                "sagaId", instance.getSagaId(),
                "state", instance.getState().name(),
                "taskId", instance.getContext().getTaskId(),
                "fromPhase", String.valueOf(instance.getContext().getFromPhase()),
                "toPhase", String.valueOf(instance.getContext().getToPhase()),
                "createdAt", instance.getCreatedAt().toString()));
    }
}
