package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;
import org.example.projectrealizationservice.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    @PostMapping
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
    public ResponseEntity<?> getWorkflowById(@PathVariable String workflowId) {
        try {
            return ResponseEntity.ok(workflowService.getWorkflowById(workflowId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{workflowId}")
    public ResponseEntity<?> updateWorkflow(@PathVariable String workflowId, @RequestBody WorkflowCreationDTO workflow) {
        try {
            workflowService.updateWorkflow(workflowId, workflow);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{workflowId}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable String workflowId) {
        try {
            workflowService.deleteWorkflow(workflowId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
