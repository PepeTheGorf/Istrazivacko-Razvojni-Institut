package org.example.projectrealizationservice.saga.graph;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.saga.PhaseTransitionSagaContext;
import org.example.projectrealizationservice.saga.graph.model.MemberNode;
import org.example.projectrealizationservice.saga.graph.model.PhaseTransitionNode;
import org.example.projectrealizationservice.saga.graph.model.ProjectNode;
import org.example.projectrealizationservice.saga.graph.model.TaskNode;
import org.example.projectrealizationservice.saga.graph.repository.PhaseTransitionNodeRepository;
import org.example.projectrealizationservice.saga.graph.repository.TaskNodeRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Profile("!test")
@RequiredArgsConstructor
@Transactional(transactionManager = "neo4jTransactionManager")
public class TaskGraphService {

    private final TaskNodeRepository taskNodeRepository;
    private final PhaseTransitionNodeRepository phaseTransitionNodeRepository;

    public String recordTransition(PhaseTransitionSagaContext context) {
        TaskNode task = taskNodeRepository.findById(context.getTaskId())
                .orElseGet(() -> TaskNode.builder().id(context.getTaskId()).build());

        task.setName(context.getTaskName());
        task.setCurrentPhase(context.getToPhase());
        task.setProject(ProjectNode.builder()
                .name(context.getProjectName())
                .displayName(context.getProjectName())
                .build());

        if (context.getAssigneeId() != null) {
            task.setAssignee(MemberNode.builder()
                    .id(String.valueOf(context.getAssigneeId()))
                    .displayName("User " + context.getAssigneeId())
                    .build());
        }

        PhaseTransitionNode transition = PhaseTransitionNode.builder()
                .id(UUID.randomUUID().toString())
                .fromPhase(context.getFromPhase())
                .toPhase(context.getToPhase())
                .durationSeconds(context.getDurationSeconds())
                .performedBy(context.getAssigneeId())
                .recordedAt(OffsetDateTime.now().toString())
                .build();

        task.getTransitions().add(transition);
        taskNodeRepository.save(task);
        return transition.getId();
    }

    public void removeTransition(String transitionId) {
        if (transitionId != null) {
            phaseTransitionNodeRepository.deleteById(transitionId);
        }
    }
}
