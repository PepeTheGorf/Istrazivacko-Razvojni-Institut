package org.example.documentmanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.AutoTagRequestDTO;
import org.example.documentmanagementservice.dto.AutoTagResponseDTO;
import org.example.documentmanagementservice.dto.SearchDocumentsRequestDTO;
import org.example.documentmanagementservice.dto.SearchDocumentsResponseDTO;
import org.example.documentmanagementservice.service.AutoTagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class AutoTagController {

    private final AutoTagService autoTagService;

    @PostMapping("/search-documents")
    public ResponseEntity<SearchDocumentsResponseDTO> searchDocuments(
            @Valid @RequestBody SearchDocumentsRequestDTO request) {
        return ResponseEntity.ok(autoTagService.searchDocuments(request));
    }

    @PostMapping("/auto-tag")
    public ResponseEntity<AutoTagResponseDTO> autoTag(
            @Valid @RequestBody AutoTagRequestDTO request) {
        return ResponseEntity.ok(autoTagService.autoTag(request));
    }
}
