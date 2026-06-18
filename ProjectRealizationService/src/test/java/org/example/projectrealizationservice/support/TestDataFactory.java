package org.example.projectrealizationservice.support;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.model.*;
import org.example.projectrealizationservice.repository.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class TestDataFactory {

    public static final String CONDITION_TASK_ASSIGNED =
            "Zadatak dodeljen korisniku";
    public static final String CONDITION_ALL_ACCEPTANCE_CRITERIA_MET =
            "Svi kriterijumi prihvatanja ispunjeni";
    public static final String CONDITION_ALL_SUBTASKS_COMPLETED =
            "Svi podzadaci završeni";
    public static final String CONDITION_NO_OPEN_PROBLEMS =
            "Nema otvorenih problema";
    public static final String CONDITION_WITHIN_DEADLINE =
            "Zadatak u roku";

    private final ProjectRepository projectRepository;
    private final WorkflowRepository workflowRepository;
    private final PhaseRepository phaseRepository;
    private final TransitionConditionTypeRepository transitionConditionTypeRepository;
    private final TransitionConditionRepository transitionConditionRepository;
    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final ProblemReportRepository problemReportRepository;

    public record PhaseTransitionSetup(
            Project project,
            Workflow workflow,
            Phase fromPhase,
            Phase toPhase,
            TransitionCondition transitionCondition
    ) {}

    public PhaseTransitionSetup createPhaseTransitionSetup(Long creatorId, String conditionTypeName) {
        Project project = projectRepository.save(Project.builder()
                .name("project-" + UUID.randomUUID())
                .creatorId(creatorId)
                .build());

        Workflow workflow = workflowRepository.save(Workflow.builder()
                .name("workflow-" + UUID.randomUUID())
                .creatorId(creatorId)
                .build());

        Phase fromPhase = phaseRepository.save(Phase.builder()
                .workflow(workflow)
                .name("Todo")
                .order(1)
                .build());

        Phase toPhase = phaseRepository.save(Phase.builder()
                .workflow(workflow)
                .name("Done")
                .order(2)
                .build());

        TransitionConditionType conditionType = transitionConditionTypeRepository.findByName(conditionTypeName)
                .orElseThrow(() -> new IllegalStateException("Missing transition condition type: " + conditionTypeName));

        TransitionCondition transitionCondition = transitionConditionRepository.save(TransitionCondition.builder()
                .workflow(workflow)
                .fromPhase(fromPhase)
                .toPhase(toPhase)
                .transitionType(conditionType)
                .build());

        return new PhaseTransitionSetup(project, workflow, fromPhase, toPhase, transitionCondition);
    }

    public TransitionCondition addTransitionCondition(PhaseTransitionSetup setup, String conditionTypeName) {
        TransitionConditionType conditionType = transitionConditionTypeRepository.findByName(conditionTypeName)
                .orElseThrow(() -> new IllegalStateException("Missing transition condition type: " + conditionTypeName));

        return transitionConditionRepository.save(TransitionCondition.builder()
                .workflow(setup.workflow())
                .fromPhase(setup.fromPhase())
                .toPhase(setup.toPhase())
                .transitionType(conditionType)
                .build());
    }

    public Task createTaskInPhase(PhaseTransitionSetup setup, Long creatorId, OffsetDateTime endDate) {
        return taskRepository.save(Task.builder()
                .name("task-" + UUID.randomUUID())
                .creatorId(creatorId)
                .project(setup.project())
                .workflow(setup.workflow())
                .phase(setup.fromPhase())
                .startDate(OffsetDateTime.now())
                .endDate(endDate)
                .phaseChangeDate(OffsetDateTime.now())
                .build());
    }

    public Task createSubtaskInPhase(Task parent, Phase phase, Long creatorId, OffsetDateTime endDate) {
        return taskRepository.save(Task.builder()
                .name("subtask-" + UUID.randomUUID())
                .creatorId(creatorId)
                .project(parent.getProject())
                .workflow(parent.getWorkflow())
                .parentTask(parent)
                .phase(phase)
                .startDate(OffsetDateTime.now())
                .endDate(endDate)
                .phaseChangeDate(OffsetDateTime.now())
                .build());
    }

    public void assignTaskToUser(Task task, Long assigneeId) {
        taskAssignmentRepository.save(TaskAssignment.builder()
                .task(task)
                .assigneeId(assigneeId)
                .assignedAt(OffsetDateTime.now())
                .build());
    }

    public AcceptanceCriteria createAcceptanceCriteria(Task task, Long creatorId, boolean completed) {
        return acceptanceCriteriaRepository.save(AcceptanceCriteria.builder()
                .task(task)
                .name("criterion-" + UUID.randomUUID())
                .creatorId(creatorId)
                .completed(completed)
                .build());
    }

    public ProblemReport createProblemReport(Task task, Long creatorId, ProblemStatus status) {
        return problemReportRepository.save(ProblemReport.builder()
                .task(task)
                .creatorId(creatorId)
                .description("problem-" + UUID.randomUUID())
                .problemType(ProblemType.TECHNICAL)
                .status(status)
                .reportedAt(OffsetDateTime.now())
                .build());
    }
}
