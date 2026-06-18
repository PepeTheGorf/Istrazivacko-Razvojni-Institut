package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.model.Phase;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TransitionCondition;
import org.example.projectrealizationservice.repository.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.PhaseRepository;
import org.example.projectrealizationservice.repository.ProblemReportRepository;
import org.example.projectrealizationservice.repository.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.TransitionConditionEvaluator;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class TransitionConditionEvaluatorImpl implements TransitionConditionEvaluator {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final ProblemReportRepository problemReportRepository;
    private final TaskRepository taskRepository;
    private final PhaseRepository phaseRepository;
    
    
    @Override
    public boolean evaluateCondition(TransitionCondition transitionCondition, Task task) {
        return switch (transitionCondition.getTransitionType().getName()) {
            case "Zadatak dodeljen korisniku" ->
                    taskAssignmentRepository.existsByTaskAndAssigneeId(task, SecurityUtils.getCurrentUserId());
            case "Svi kriterijumi prihvatanja ispunjeni" ->
                    Boolean.TRUE.equals(acceptanceCriteriaRepository.allAcceptanceCriteriaMetByTaskId(task.getId()));
            case "Svi podzadaci završeni" -> allSubtasksCompleted(task);
            case "Nema otvorenih problema" -> problemReportRepository.countUnresolvedProblems(task) == 0;
            case "Zadatak u roku" -> task.getEndDate().isAfter(OffsetDateTime.now());
            default ->
                    throw new IllegalStateException("Unexpected value: " + transitionCondition.getTransitionType().getName());
        };
    }

    @Override
    public boolean evaluateConditions(List<TransitionCondition> conditions, Task task) {
        return conditions.stream().allMatch(condition -> evaluateCondition(condition, task));
    }
    
    private boolean allSubtasksCompleted(Task task) {
        Queue<Task> subtasks = new LinkedList<>(taskRepository.findSubtasksByParentTaskId(task.getId()));

        while (!subtasks.isEmpty()) {
            Task currentTask = subtasks.poll();

            if (!isInFinalPhase(currentTask)) {
                return false;
            }

            subtasks.addAll(taskRepository.findSubtasksByParentTaskId(currentTask.getId()));
        }
        return true;
    }

    private boolean isInFinalPhase(Task task) {
        Phase lastPhase = phaseRepository.findFirstByWorkflow_IdOrderByOrderDesc(task.getWorkflow().getId())
                .orElseThrow(() -> new IllegalStateException("No phases defined for workflow " + task.getWorkflow().getId()));
        return lastPhase.getId().equals(task.getPhase().getId());
    }
}
