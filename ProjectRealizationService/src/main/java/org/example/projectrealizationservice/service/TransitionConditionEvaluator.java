package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TransitionCondition;

import java.util.List;

public interface TransitionConditionEvaluator {
    boolean evaluateCondition(TransitionCondition condition, Task task);

    boolean evaluateConditions(List<TransitionCondition> conditions, Task task);
}
