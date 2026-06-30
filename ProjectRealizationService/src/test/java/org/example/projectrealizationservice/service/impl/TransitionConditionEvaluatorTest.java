package org.example.projectrealizationservice.service.impl;

import org.example.projectrealizationservice.model.*;
import org.example.projectrealizationservice.repository.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.PhaseRepository;
import org.example.projectrealizationservice.repository.ProblemReportRepository;
import org.example.projectrealizationservice.repository.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitionConditionEvaluatorTest {

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @Mock
    private AcceptanceCriteriaRepository acceptanceCriteriaRepository;

    @Mock
    private ProblemReportRepository problemReportRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PhaseRepository phaseRepository;

    @InjectMocks
    private TransitionConditionEvaluatorImpl evaluator;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void evaluateConditions_deadlineMet_returnsTrue() {
        Task task = Task.builder()
                .id(1L)
                .endDate(OffsetDateTime.now().plusDays(1))
                .build();

        TransitionCondition condition = conditionWithName("Zadatak u roku");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isTrue();
    }

    @Test
    void evaluateConditions_deadlineMissed_returnsFalse() {
        Task task = Task.builder()
                .id(1L)
                .endDate(OffsetDateTime.now().minusDays(1))
                .build();

        TransitionCondition condition = conditionWithName("Zadatak u roku");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isFalse();
    }

    @Test
    void evaluateConditions_taskAssignedToCurrentUser_returnsTrue() {
        setCurrentUser(42L);
        Task task = Task.builder().id(1L).build();
        when(taskAssignmentRepository.existsByTaskAndAssigneeId(task, 42L)).thenReturn(true);

        TransitionCondition condition = conditionWithName("Zadatak dodeljen korisniku");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isTrue();
    }

    @Test
    void evaluateConditions_noOpenProblems_returnsTrue() {
        Task task = Task.builder().id(1L).build();
        when(problemReportRepository.countUnresolvedProblems(task)).thenReturn(0);

        TransitionCondition condition = conditionWithName("Nema otvorenih problema");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isTrue();
    }

    @Test
    void evaluateConditions_noAcceptanceCriteria_returnsTrue() {
        Task task = Task.builder().id(1L).build();
        when(acceptanceCriteriaRepository.allAcceptanceCriteriaMetByTaskId(1L)).thenReturn(true);

        TransitionCondition condition = conditionWithName("Svi kriterijumi prihvatanja ispunjeni");

        assertThat(evaluator.evaluateCondition(condition, task)).isTrue();
    }

    @Test
    void evaluateConditions_allAcceptanceCriteriaMet_returnsTrue() {
        Task task = Task.builder().id(1L).build();
        when(acceptanceCriteriaRepository.allAcceptanceCriteriaMetByTaskId(1L)).thenReturn(true);

        TransitionCondition condition = conditionWithName("Svi kriterijumi prihvatanja ispunjeni");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isTrue();
    }

    @Test
    void evaluateConditions_acceptanceCriteriaNotMet_returnsFalse() {
        Task task = Task.builder().id(1L).build();
        when(acceptanceCriteriaRepository.allAcceptanceCriteriaMetByTaskId(1L)).thenReturn(false);

        TransitionCondition condition = conditionWithName("Svi kriterijumi prihvatanja ispunjeni");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isFalse();
    }

    @Test
    void evaluateConditions_unresolvedProblemsExist_returnsFalse() {
        Task task = Task.builder().id(1L).build();
        when(problemReportRepository.countUnresolvedProblems(task)).thenReturn(2);

        TransitionCondition condition = conditionWithName("Nema otvorenih problema");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isFalse();
    }

    @Test
    void evaluateConditions_allSubtasksInFinalPhase_returnsTrue() {
        Phase finalPhase = Phase.builder().id(20L).order(2).build();
        Workflow workflow = Workflow.builder().id(100L).build();

        Task subtask = Task.builder().id(2L).workflow(workflow).phase(finalPhase).build();
        Task task = Task.builder().id(1L).workflow(workflow).build();

        when(taskRepository.findSubtasksByParentTaskId(1L)).thenReturn(List.of(subtask));
        when(taskRepository.findSubtasksByParentTaskId(2L)).thenReturn(List.of());
        when(phaseRepository.findFirstByWorkflow_IdOrderByOrderDesc(100L)).thenReturn(Optional.of(finalPhase));

        TransitionCondition condition = conditionWithName("Svi podzadaci završeni");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isTrue();
    }

    @Test
    void evaluateConditions_subtaskNotInFinalPhase_returnsFalse() {
        Phase todoPhase = Phase.builder().id(10L).order(1).build();
        Phase finalPhase = Phase.builder().id(20L).order(2).build();
        Workflow workflow = Workflow.builder().id(100L).build();

        Task subtask = Task.builder().id(2L).workflow(workflow).phase(todoPhase).build();
        Task task = Task.builder().id(1L).workflow(workflow).build();

        when(taskRepository.findSubtasksByParentTaskId(1L)).thenReturn(List.of(subtask));
        when(phaseRepository.findFirstByWorkflow_IdOrderByOrderDesc(100L)).thenReturn(Optional.of(finalPhase));

        TransitionCondition condition = conditionWithName("Svi podzadaci završeni");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isFalse();
    }

    @Test
    void evaluateConditions_taskNotAssignedToCurrentUser_returnsFalse() {
        setCurrentUser(42L);
        Task task = Task.builder().id(1L).build();
        when(taskAssignmentRepository.existsByTaskAndAssigneeId(task, 42L)).thenReturn(false);

        TransitionCondition condition = conditionWithName("Zadatak dodeljen korisniku");

        assertThat(evaluator.evaluateConditions(List.of(condition), task)).isFalse();
    }

    @Test
    void evaluateConditions_multipleConditions_allMustPass() {
        Task task = Task.builder()
                .id(1L)
                .endDate(OffsetDateTime.now().plusDays(1))
                .build();

        when(problemReportRepository.countUnresolvedProblems(task)).thenReturn(0);

        List<TransitionCondition> conditions = List.of(
                conditionWithName("Zadatak u roku"),
                conditionWithName("Nema otvorenih problema")
        );

        assertThat(evaluator.evaluateConditions(conditions, task)).isTrue();
    }

    @Test
    void evaluateConditions_multipleConditions_failsIfOneFails() {
        Task task = Task.builder()
                .id(1L)
                .endDate(OffsetDateTime.now().minusDays(1))
                .build();

        List<TransitionCondition> conditions = List.of(
                conditionWithName("Zadatak u roku"),
                conditionWithName("Nema otvorenih problema")
        );

        assertThat(evaluator.evaluateConditions(conditions, task)).isFalse();
    }

    private static TransitionCondition conditionWithName(String name) {
        return TransitionCondition.builder()
                .transitionType(TransitionConditionType.builder().name(name).build())
                .build();
    }

    private static void setCurrentUser(Long userId) {
        var principal = new org.example.projectrealizationservice.security.JwtUserPrincipal(
                userId, "user@test.com", Role.TEAM_MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
