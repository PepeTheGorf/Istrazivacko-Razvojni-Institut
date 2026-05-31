package org.example.projectrealizationanalyticsservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projectrealizationanalyticsservice.analytics.model.PhaseTransitionEvent;
import org.example.projectrealizationanalyticsservice.analytics.model.RangeType;
import org.example.projectrealizationanalyticsservice.service.PhaseTransitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final PhaseTransitionService phaseTransitionService;
    
    @PostMapping("/transfer")
    public ResponseEntity<?> transferTaskPhase(@RequestBody PhaseTransitionEvent phaseTransitionEvent) {
        try {
            phaseTransitionService.transferTaskPhase(phaseTransitionEvent);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("phase-flow-by-project")
    public ResponseEntity<?> getPhaseFlowByProject(@RequestParam String projectName, @RequestParam RangeType rangeType) {
        try {
            long startTime = System.currentTimeMillis();
            List<PhaseTransitionEvent> result = phaseTransitionService.getPhaseFlowByProject(projectName, rangeType);
            long endTime = System.currentTimeMillis();
            log.info("getPhaseFlowByProject executed in {} ms", (endTime - startTime));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("time-spent-by-task")
    public ResponseEntity<?> getTimeSpentByTask(@RequestParam String projectName, @RequestParam RangeType rangeType) {
        try {
            return ResponseEntity.ok(phaseTransitionService.getTimeSpentByTask(projectName, rangeType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("average-duration-over-time")
    public ResponseEntity<?> getAverageDurationInPhasesByProjectOverTime(@RequestParam String projectName, @RequestParam RangeType rangeType, @RequestParam String window) {
        try {
            return ResponseEntity.ok(phaseTransitionService.getAverageDurationInPhasesByProjectOverTime(projectName, rangeType, window));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
