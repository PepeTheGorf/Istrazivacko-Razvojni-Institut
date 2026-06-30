package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.smartdocs.TemplateCreationDTO;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.SmartDocService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List; 
import org.example.projectrealizationservice.dto.smartdocs.SmartTemplateDTO;
import org.example.projectrealizationservice.dto.smartdocs.AiAnalyticsReportDTO;
import org.example.projectrealizationservice.dto.smartdocs.PromptVersionDTO;
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
        e.printStackTrace(); 
        return ResponseEntity.status(500).body("Greška na serveru: " + e.getMessage()); }
    }

    @GetMapping("/templates/all")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<SmartTemplateDTO>> getAllTemplates() {
        return ResponseEntity.ok(smartDocService.getAllTemplates());
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<?> updateSectionInput(
        @PathVariable Long sectionId, 
        @RequestBody Map<String, String> payload) {
    try {
        String text = payload.get("userInput");
        smartDocService.updateSectionInput(sectionId, text);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<?> getMyDocuments() {
    try {
        Long researcherId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(smartDocService.getMyDocuments(researcherId));
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Greška: " + e.getMessage());
    }
    }

    @PostMapping("/sections/{sectionId}/generate")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<?> generateContent(@PathVariable Long sectionId) {
    try {
        String result = smartDocService.generateSectionContent(sectionId);
        return ResponseEntity.ok(Map.of("result", result));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Greška pri generisanju: " + e.getMessage());
    }
  }

   @PostMapping("/documents/{id}/complete")
   @PreAuthorize("hasRole('TEAM_MEMBER')")
   public ResponseEntity<?> completeDocument(@PathVariable Long id) {
    try {
        smartDocService.completeDocument(id);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.status(500).body(e.getMessage());
    }
   }

    @PostMapping("/sections/{sectionId}/feedback")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<?> leaveFeedback(
        @PathVariable Long sectionId,
        @RequestBody Map<String, Object> payload) {
    try {
        Integer rating = (Integer) payload.get("rating");
        String comment = (String) payload.get("comment");
        smartDocService.saveFeedback(sectionId, rating, comment);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
   }

   @DeleteMapping("/documents/{id}")
   @PreAuthorize("hasRole('TEAM_MEMBER')")
   public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
    try {
        smartDocService.deleteDocument(id);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Greška pri brisanju: " + e.getMessage());
    }
   }


   @GetMapping("/sections/{sectionId}/prompts")
   @PreAuthorize("hasRole('MANAGER')")
   public ResponseEntity<List<PromptVersionDTO>> getPromptHistory(@PathVariable Long sectionId) {
    return ResponseEntity.ok(smartDocService.getPromptHistory(sectionId));
   }

   @PostMapping("/sections/{sectionId}/prompts")
   @PreAuthorize("hasRole('MANAGER')")
   public ResponseEntity<?> createNewVersion(
        @PathVariable Long sectionId, 
        @RequestBody Map<String, String> payload) {
    try {
        String newContent = payload.get("content");
        smartDocService.createNewPromptVersion(sectionId, newContent);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

   @PutMapping("/sections/{sectionId}/prompts/{versionId}/activate")
   @PreAuthorize("hasRole('MANAGER')")
   public ResponseEntity<?> activateVersion(
        @PathVariable Long sectionId, 
        @PathVariable Long versionId) {
    try {
        smartDocService.activateOldVersion(sectionId, versionId);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/templates/{id}")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<SmartTemplateDTO> getTemplateById(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(smartDocService.getTemplateById(id));
    } catch (Exception e) {
        return ResponseEntity.status(404).body(null);
    }
}

@PutMapping("/sections/{sectionId}/refined")
@PreAuthorize("hasRole('TEAM_MEMBER')")
public ResponseEntity<?> updateRefinedResult(
        @PathVariable Long sectionId, 
        @RequestBody Map<String, String> payload) {
    try {
        String text = payload.get("refinedResult");
        smartDocService.updateRefinedResult(sectionId, text);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

@GetMapping("/reports/ai-analytics")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<List<AiAnalyticsReportDTO>> getAiReport(
        @RequestParam String startDate, 
        @RequestParam String endDate) {
    return ResponseEntity.ok(smartDocService.getAiAnalyticsReport(
            OffsetDateTime.parse(startDate), 
            OffsetDateTime.parse(endDate)));

}
}