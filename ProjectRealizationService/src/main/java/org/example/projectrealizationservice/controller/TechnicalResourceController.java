package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;
import org.example.projectrealizationservice.service.TechnicalResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/technical-resources")
@RequiredArgsConstructor
public class TechnicalResourceController {
    private final TechnicalResourceService technicalResourceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createTechnicalResource(@RequestBody TechnicalResourceCreationDTO technicalResourceCreationDTO) {
        try {
            technicalResourceService.createTechnicalResource(technicalResourceCreationDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/assign/{resourceId}/to-task/{taskId}/quantity/{quantity}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> assignTechnicalResourceToTask(@PathVariable Long resourceId, @PathVariable Long taskId, @PathVariable Integer quantity) {
        try {
            technicalResourceService.assignTechnicalResourceToTask(resourceId, taskId, quantity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTechnicalResourcesByTaskId(@PathVariable Long taskId) {
        try {
            return ResponseEntity.ok(technicalResourceService.getTechnicalResourcesByTaskId(taskId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTechnicalResources() {
        try {
            return ResponseEntity.ok(technicalResourceService.getAllTechnicalResources());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{technicalResourceName}")
    public ResponseEntity<?> getTechnicalResourceById(@PathVariable String technicalResourceName) {
        try {
            return ResponseEntity.ok(technicalResourceService.getTechnicalResourceByName(technicalResourceName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{technicalResourceId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> updateTechnicalResource(@PathVariable Long technicalResourceId,
                                                     @RequestBody TechnicalResourceCreationDTO technicalResourceCreationDTO) {
        try {
            technicalResourceService.updateTechnicalResource(technicalResourceId, technicalResourceCreationDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{technicalResourceId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> deleteTechnicalResource(@PathVariable Long technicalResourceId) {
        try {
            technicalResourceService.deleteTechnicalResource(technicalResourceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
