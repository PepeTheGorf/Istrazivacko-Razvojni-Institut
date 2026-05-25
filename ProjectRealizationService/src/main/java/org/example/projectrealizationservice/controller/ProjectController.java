package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectDTO;
import org.example.projectrealizationservice.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO project) {
        try {
            projectService.createProject(project);
            return ResponseEntity.ok().build();
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
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody ProjectDTO project) {
        try {
            projectService.updateProject(projectId, project);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId) {
        try {
            projectService.deleteProject(projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
