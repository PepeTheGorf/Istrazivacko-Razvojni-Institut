package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectDTO;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.ProjectService;
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
            Long creatorId = SecurityUtils.getCurrentUserId();
            projectService.createProject(project, creatorId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public List<ProjectDTO> getAllProjects() {
        Long creatorId = SecurityUtils.getCurrentUserId();
        return projectService.findAll(creatorId);
    }

    @GetMapping("/{projectId}")
    public ProjectDTO getProjectById(@PathVariable String projectId) {
        Long creatorId = SecurityUtils.getCurrentUserId();
        return projectService.getProjectById(projectId, creatorId);
    }
    
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody ProjectDTO project) {
        try {
            Long creatorId = SecurityUtils.getCurrentUserId();
            projectService.updateProject(projectId, project, creatorId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId) {
        try {
            Long creatorId = SecurityUtils.getCurrentUserId();
            projectService.deleteProject(projectId, creatorId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
