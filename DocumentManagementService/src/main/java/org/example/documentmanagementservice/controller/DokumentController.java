package org.example.documentmanagementservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.DokumentRequestDTO;
import org.example.documentmanagementservice.model.Dokument;
import org.example.documentmanagementservice.service.DocumentService;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/dokumenti")
@RequiredArgsConstructor
@Validated
public class DokumentController {

    private final DocumentService documentService;

    @PostMapping("")
    public ResponseEntity<Dokument> create(@Valid @RequestBody DokumentRequestDTO request) {
        Dokument created = documentService.create(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("")
    public ResponseEntity<List<Dokument>> listAll() {
        return ResponseEntity.ok(documentService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dokument> getById(@PathVariable java.util.UUID id) {
        Dokument d = documentService.getById(id);
        return ResponseEntity.ok(d);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dokument> update(@PathVariable java.util.UUID id, @Valid @RequestBody DokumentRequestDTO request) {
        Dokument updated = documentService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
