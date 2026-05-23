package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;
import org.example.projectrealizationservice.service.ProblemReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task-problems")
@RequiredArgsConstructor
public class TaskProblemController {
    private ProblemReportService problemReportService;
    
    @PostMapping
    public ResponseEntity<?> reportProblem(@RequestBody ProblemReportCreationDTO problemReport) {
        try {
            problemReportService.createProblemReport(problemReport);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getProblemsByTask(@RequestParam String taskId) {
        try {
            return ResponseEntity.ok(problemReportService.getAllProblemsByTask(taskId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{problemReportId}")
    public ResponseEntity<?> getProblemReportById(@PathVariable String problemReportId) {
        try {
            return ResponseEntity.ok(problemReportService.getProblemReportById(problemReportId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{problemReportId}")
    public ResponseEntity<?> updateProblemReport(@PathVariable String problemReportId, @RequestBody ProblemReportCreationDTO problemReport) {
        try {
            problemReportService.updateProblemReport(problemReportId, problemReport);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{problemReportId}")
    public ResponseEntity<?> deleteProblemReport(@PathVariable String problemReportId) {
        try {
            problemReportService.deleteProblemReport(problemReportId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
