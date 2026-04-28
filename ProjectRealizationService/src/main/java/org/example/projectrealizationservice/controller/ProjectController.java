package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectDTO;
import org.example.projectrealizationservice.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProject(@RequestBody ProjectDTO project) {
        projectService.createProject(project);
    }
    
    @GetMapping("/analytics/workflow-final-phase")
    public ResponseEntity<?> findProjectsByWorkflowWithMinTaskCount(
            @RequestParam String workflowName,
            @RequestParam long minTaskCount) {
        try {
            return ResponseEntity.ok(projectService.findProjectsByWorkflowWithMinTaskCount(workflowName, minTaskCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/analytics/delayed-tasks")
    public ResponseEntity<?> findProjectsWithDelayedTasks(
            @RequestParam String currentDate) {
        try {
            return ResponseEntity.ok(projectService.findProjectsWithDelayedTasks(OffsetDateTime.parse(currentDate)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/analytics/resource-workload")
    public ResponseEntity<?> findProjectsWithHighTechnicalResourceWorkload(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            return ResponseEntity.ok(
                    projectService.findProjectsWithHighTechnicalResourceWorkload(
                            OffsetDateTime.parse(startDate),
                            OffsetDateTime.parse(endDate)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public List<ProjectDTO> getAllProjects() {
        return projectService.findAll();
    }

    @GetMapping("/{projectId}")
    public ProjectDTO getProjectById(@PathVariable String projectId) {
        return projectService.getProjectById(projectId);
    }
    
    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody ProjectDTO project) {
        try {
            projectService.updateProject(projectId, project);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId) {
        try {
            projectService.deleteProject(projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
