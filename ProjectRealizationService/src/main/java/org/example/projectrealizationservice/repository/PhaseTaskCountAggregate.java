package org.example.projectrealizationservice.repository;

public interface PhaseTaskCountAggregate {
    Long getPhaseId();

    String getPhaseName();

    Integer getPhaseOrder();

    Long getTaskCount();
}
