package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;
import org.example.projectrealizationservice.service.TechnicalResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/technical-resources")
@RequiredArgsConstructor
public class TechnicalResourceController {
    private final TechnicalResourceService technicalResourceService;

    @PostMapping
    public ResponseEntity<?> createTechnicalResource(@RequestBody TechnicalResourceCreationDTO technicalResourceCreationDTO) {
        try {
            technicalResourceService.createTechnicalResource(technicalResourceCreationDTO);
            return ResponseEntity.ok().build();
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
    public ResponseEntity<?> updateTechnicalResource(@PathVariable String technicalResourceId,
                                                     @RequestBody TechnicalResourceCreationDTO technicalResourceCreationDTO) {
        try {
            technicalResourceService.updateTechnicalResource(technicalResourceId, technicalResourceCreationDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{technicalResourceId}")
    public ResponseEntity<?> deleteTechnicalResource(@PathVariable String technicalResourceId) {
        try {
            technicalResourceService.deleteTechnicalResource(technicalResourceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
