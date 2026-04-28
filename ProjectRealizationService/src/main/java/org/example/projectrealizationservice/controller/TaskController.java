package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskCreationDTO taskCreationDTO) {
        try {
            taskService.createTask(taskCreationDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProjectId(@PathVariable String projectId) {
        try {
            return ResponseEntity.ok(taskService.getTasksByProjectId(projectId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/next-phase/{taskId}")
    public ResponseEntity<?> moveTaskToNextPhase(@PathVariable String taskId) {
        try {
            taskService.moveTaskToNextPhase(taskId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/acceptance-criteria/{taskId}/{criteriaId}")
    public ResponseEntity<?> completeAcceptanceCriteria(@PathVariable String taskId, @PathVariable String criteriaId) {
        try {
            taskService.completeAcceptanceCriteria(taskId, criteriaId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/analytics/acceptance-criteria-completion")
    public ResponseEntity<?> findTasksWithUncompletedAcceptanceCriteria(
            @RequestParam String projectId,
            @RequestParam String phaseId,
            @RequestParam long minCompletedCriteria) {
        try {
            return ResponseEntity.ok(taskService.analyzeAcceptanceCriteriaCompletion(projectId, phaseId, minCompletedCriteria));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable String taskId) {
        try {
            return ResponseEntity.ok(taskService.getTaskById(taskId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable String taskId, @RequestBody TaskCreationDTO taskCreationDTO) {
        try {
            taskService.updateTask(taskId, taskCreationDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable String taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
