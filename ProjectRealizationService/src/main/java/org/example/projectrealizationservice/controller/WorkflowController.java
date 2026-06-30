package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;
import org.example.projectrealizationservice.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createWorkflow(@RequestBody WorkflowCreationDTO workflow) {
        try {
            workflowService.createWorkflow(workflow);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<?> getWorkflowByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(workflowService.getWorkflowByName(name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<?> getWorkflowById(@PathVariable Long workflowId) {
        try {
            return ResponseEntity.ok(workflowService.getWorkflowById(workflowId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{workflowId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> updateWorkflow(@PathVariable Long workflowId, @RequestBody WorkflowCreationDTO workflow) {
        try {
            workflowService.updateWorkflow(workflowId, workflow);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{workflowId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> deleteWorkflow(@PathVariable Long workflowId) {
        try {
            workflowService.deleteWorkflow(workflowId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/transition-condition-types")
    public ResponseEntity<?> getAllTransitionConditionTypes() {
        try {
            return ResponseEntity.ok(workflowService.getAllTransitionConditionTypes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllWorkflows() {
        try {
            return ResponseEntity.ok(workflowService.getAllWorkflows());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
