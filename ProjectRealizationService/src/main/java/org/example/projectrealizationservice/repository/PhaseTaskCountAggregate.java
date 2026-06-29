package org.example.projectrealizationservice.repository;

public interface PhaseTaskCountAggregate {
    String getPhaseName();

    Integer getPhaseOrder();

    Long getTaskCount();
}
