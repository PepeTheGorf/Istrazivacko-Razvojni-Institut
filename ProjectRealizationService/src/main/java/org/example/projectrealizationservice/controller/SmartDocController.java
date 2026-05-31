package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.smartdocs.TemplateCreationDTO;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.SmartDocService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/smart-docs")
@RequiredArgsConstructor
public class SmartDocController {

    private final SmartDocService smartDocService;

    // Menadžer
    @PostMapping("/templates")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createTemplate(@RequestBody TemplateCreationDTO dto) {
        try {
            Long creatorId = SecurityUtils.getCurrentUserId();
            smartDocService.createTemplate(dto, creatorId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates(@RequestParam Long domainId, @RequestParam Long categoryId) {
        return ResponseEntity.ok(smartDocService.getTemplatesByFilter(domainId, categoryId));
    }

    @PostMapping("/documents")
    public ResponseEntity<?> createDocument(@RequestBody Map<String, Long> payload) {
        try {
            Long templateId = payload.get("templateId");
            Long researcherId = SecurityUtils.getCurrentUserId();
            return ResponseEntity.ok(smartDocService.createDocumentFromTemplate(templateId, researcherId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/domains")
    public ResponseEntity<?> getDomains() {
        return ResponseEntity.ok(smartDocService.getAllDomains());
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(smartDocService.getAllCategories());
    }
    
    @GetMapping("/documents/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(smartDocService.getDocumentById(id));
    } catch (Exception e) {
return ResponseEntity.status(404).body("Dokument nije pronađen");    }
}
@GetMapping("/templates/all")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<?> getAllTemplates() {
    return ResponseEntity.ok(smartDocService.getAllTemplates());
}
}