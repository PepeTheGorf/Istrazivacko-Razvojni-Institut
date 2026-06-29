package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;
import org.example.projectrealizationservice.service.ProblemReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task-problems")
@RequiredArgsConstructor
public class TaskProblemController {
    private final ProblemReportService problemReportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<?> reportProblem(@RequestBody ProblemReportCreationDTO problemReport) {
        try {
            problemReportService.createProblemReport(problemReport.getTaskId(), problemReport);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<?> getMyProblemReports() {
        try {
            return ResponseEntity.ok(problemReportService.getMyProblemReports());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getProblemsByTask(@RequestParam Long taskId) {
        try {
            return ResponseEntity.ok(problemReportService.getAllProblemsByTask(taskId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{problemReportId}")
    public ResponseEntity<?> getProblemReportById(@PathVariable Long problemReportId) {
        try {
            return ResponseEntity.ok(problemReportService.getProblemReportById(problemReportId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{problemReportId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<?> updateProblemReport(@PathVariable Long problemReportId,
                                                 @RequestBody ProblemReportCreationDTO problemReport) {
        try {
            problemReportService.updateProblemReport(problemReportId, problemReport);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{problemReportId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<?> deleteProblemReport(@PathVariable Long problemReportId) {
        try {
            problemReportService.deleteProblemReport(problemReportId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
