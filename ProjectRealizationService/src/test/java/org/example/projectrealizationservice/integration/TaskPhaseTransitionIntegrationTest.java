package org.example.projectrealizationservice.integration;

import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.ProblemStatus;
import org.example.projectrealizationservice.model.Role;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.support.AbstractIntegrationTest;
import org.example.projectrealizationservice.support.TestDataFactory;
import org.example.projectrealizationservice.support.TestDataFactory.PhaseTransitionSetup;
import org.example.projectrealizationservice.support.TestJwtSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.projectrealizationservice.support.TestDataFactory.CONDITION_ALL_ACCEPTANCE_CRITERIA_MET;
import static org.example.projectrealizationservice.support.TestDataFactory.CONDITION_ALL_SUBTASKS_COMPLETED;
import static org.example.projectrealizationservice.support.TestDataFactory.CONDITION_NO_OPEN_PROBLEMS;
import static org.example.projectrealizationservice.support.TestDataFactory.CONDITION_TASK_ASSIGNED;
import static org.example.projectrealizationservice.support.TestDataFactory.CONDITION_WITHIN_DEADLINE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskPhaseTransitionIntegrationTest extends AbstractIntegrationTest {

    private static final String TRANSITION_FAILED_MESSAGE =
            "Transition conditions not met for moving task to the next phase.";

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private TaskRepository taskRepository;

    // --- Zadatak u roku ---

    @Test
    void moveToPhase_succeedsWhenDeadlineConditionMet() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_WITHIN_DEADLINE);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    @Test
    void moveToPhase_failsWhenDeadlineExceeded() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_WITHIN_DEADLINE);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().minusDays(1));

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    // --- Zadatak dodeljen korisniku ---

    @Test
    void moveToPhase_succeedsWhenTaskAssignedToCurrentUser() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_TASK_ASSIGNED);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.assignTaskToUser(task, USER_ID);

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    @Test
    void moveToPhase_failsWhenTaskNotAssignedToCurrentUser() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_TASK_ASSIGNED);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    @Test
    void moveToPhase_failsWhenTaskAssignedToAnotherUser() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_TASK_ASSIGNED);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.assignTaskToUser(task, 99L);

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    // --- Svi kriterijumi prihvatanja ispunjeni ---

    @Test
    void moveToPhase_succeedsWhenAllAcceptanceCriteriaMet() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_ALL_ACCEPTANCE_CRITERIA_MET);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createAcceptanceCriteria(task, USER_ID, true);
        testDataFactory.createAcceptanceCriteria(task, USER_ID, true);

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    @Test
    void moveToPhase_failsWhenAcceptanceCriteriaIncomplete() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_ALL_ACCEPTANCE_CRITERIA_MET);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createAcceptanceCriteria(task, USER_ID, true);
        testDataFactory.createAcceptanceCriteria(task, USER_ID, false);

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    // --- Svi podzadaci završeni ---

    @Test
    void moveToPhase_succeedsWhenAllSubtasksInFinalPhase() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_ALL_SUBTASKS_COMPLETED);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createSubtaskInPhase(task, setup.toPhase(), USER_ID, OffsetDateTime.now().plusDays(1));

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    @Test
    void moveToPhase_failsWhenSubtaskNotInFinalPhase() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_ALL_SUBTASKS_COMPLETED);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createSubtaskInPhase(task, setup.fromPhase(), USER_ID, OffsetDateTime.now().plusDays(1));

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    @Test
    void moveToPhase_succeedsWhenTaskHasNoSubtasks() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_ALL_SUBTASKS_COMPLETED);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    // --- Nema otvorenih problema ---

    @Test
    void moveToPhase_succeedsWhenNoProblemsReported() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_NO_OPEN_PROBLEMS);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    @Test
    void moveToPhase_succeedsWhenAllProblemsResolved() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_NO_OPEN_PROBLEMS);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createProblemReport(task, USER_ID, ProblemStatus.RESOLVED);

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    @Test
    void moveToPhase_failsWhenOpenProblemExists() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_NO_OPEN_PROBLEMS);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createProblemReport(task, USER_ID, ProblemStatus.OPEN);

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    @Test
    void moveToPhase_failsWhenInProgressProblemExists() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_NO_OPEN_PROBLEMS);
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createProblemReport(task, USER_ID, ProblemStatus.IN_PROGRESS);

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    @Test
    void moveToPhase_failsWhenOnlyOneOfMultipleConditionsMet() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_WITHIN_DEADLINE);
        testDataFactory.addTransitionCondition(setup, CONDITION_NO_OPEN_PROBLEMS);

        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));
        testDataFactory.createProblemReport(task, USER_ID, ProblemStatus.OPEN);

        performMove(task, setup)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(TRANSITION_FAILED_MESSAGE));
    }

    @Test
    void moveToPhase_succeedsWhenAllMultipleConditionsMet() throws Exception {
        var setup = testDataFactory.createPhaseTransitionSetup(USER_ID, CONDITION_WITHIN_DEADLINE);
        testDataFactory.addTransitionCondition(setup, CONDITION_NO_OPEN_PROBLEMS);

        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(1));

        performMove(task, setup).andExpect(status().isOk());

        assertPhaseUpdated(task, setup.toPhase());
    }

    private org.springframework.test.web.servlet.ResultActions performMove(Task task, PhaseTransitionSetup setup)
            throws Exception {
        return mockMvc.perform(put("/tasks/{taskId}/move-to-phase/{phaseId}", task.getId(), setup.toPhase().getId())
                .with(TestJwtSupport.bearer(USER_ID, Role.TEAM_MEMBER)));
    }

    private void assertPhaseUpdated(Task task, Phase expectedPhase) {
        Task updated = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updated.getPhase().getId()).isEqualTo(expectedPhase.getId());
    }
}
