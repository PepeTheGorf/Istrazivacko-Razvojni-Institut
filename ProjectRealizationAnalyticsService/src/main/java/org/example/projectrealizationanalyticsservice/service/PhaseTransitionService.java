package org.example.projectrealizationanalyticsservice.service;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationanalyticsservice.analytics.model.PhaseTransitionEvent;
import org.example.projectrealizationanalyticsservice.analytics.model.RangeType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseTransitionService {

    private final InfluxService influxService;

    public void transferTaskPhase(PhaseTransitionEvent phaseTransitionEvent) {
        influxService.logPhaseTransition(phaseTransitionEvent);
    }
    
    public List<PhaseTransitionEvent> getPhaseFlowByProject(String projectName, RangeType rangeType) {
        return influxService.getTransitionsBetweenPhases(projectName, rangeType);
    }
    
    public List<PhaseTransitionEvent> getTimeSpentByTask(String projectName, RangeType rangeType) {
        return influxService.getTimeSpentByTask(projectName, rangeType);
    }
    
    public List<PhaseTransitionEvent> getAverageDurationInPhasesByProjectOverTime(String projectName, RangeType rangeType, String window) {
        return influxService.getAverageDurationInPhasesByProjectOverTime(projectName, rangeType, window);
    }
}
