package org.example.projectrealizationservice.repository;

public interface TaskDurationAggregate {
    Long getTaskId();

    Long getTotalDurationSeconds();
}
