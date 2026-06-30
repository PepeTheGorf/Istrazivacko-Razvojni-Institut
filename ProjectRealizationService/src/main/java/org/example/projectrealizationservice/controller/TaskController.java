package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.TaskAssignmentDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PutMapping("/assign-member")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> assignTaskToUser(@RequestBody TaskAssignmentDTO taskAssignmentDTO) {
        try {
            taskService.assignTaskToUser(taskAssignmentDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{taskId}/move-to-phase/{phaseId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<?> moveTaskToNextPhase(@PathVariable Long taskId, @PathVariable Long phaseId) {
        try {
            taskService.moveTaskToNextPhase(taskId, phaseId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createTask(@RequestBody TaskCreationDTO taskCreationDTO) {
        try {
            return ResponseEntity.ok(taskService.createTask(taskCreationDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProjectId(@PathVariable Long projectId) {
        try {
            return ResponseEntity.ok(taskService.getTasksByProjectId(projectId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<?> getMyProjects() {
        try {
            return ResponseEntity.ok(taskService.getMyProjects());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/transitions/{taskId}")
    @PreAuthorize("hasRole('TEAM_MEMBER') or hasRole('MANAGER')")
    public ResponseEntity<?> getTaskTransitions(@PathVariable Long taskId) {
        try {
            return ResponseEntity.ok(taskService.getTaskTransitions(taskId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-tasks/{projectId}")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<?> getMyTasksByProjectId(@PathVariable Long projectId) {
        try {
            return ResponseEntity.ok(taskService.getMyTasksByProjectId(projectId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable Long taskId) {
        try {
            return ResponseEntity.ok(taskService.getTaskById(taskId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId, @RequestBody TaskCreationDTO taskCreationDTO) {
        try {
            taskService.updateTask(taskId, taskCreationDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
