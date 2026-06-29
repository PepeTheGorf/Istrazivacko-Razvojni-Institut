package org.example.documentmanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.DokumentTagBulkRequestDTO;
import org.example.documentmanagementservice.dto.DokumentTagRequestDTO;
import org.example.documentmanagementservice.dto.DokumentTagResponseDTO;
import org.example.documentmanagementservice.service.DokumentTagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dokument-tag")
@RequiredArgsConstructor
public class DokumentTagController {

    private final DokumentTagService service;

    @GetMapping
    public ResponseEntity<List<DokumentTagResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{dokumentId}/{tagId}")
    public ResponseEntity<DokumentTagResponseDTO> getById(@PathVariable UUID dokumentId, @PathVariable UUID tagId) {
        return ResponseEntity.ok(service.findById(dokumentId, tagId));
    }

    @PostMapping
    public ResponseEntity<DokumentTagResponseDTO> create(@Valid @RequestBody DokumentTagRequestDTO request) {
        DokumentTagResponseDTO created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{dokumentId}/{tagId}")
    public ResponseEntity<Void> delete(@PathVariable UUID dokumentId, @PathVariable UUID tagId) {
        service.delete(dokumentId, tagId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dokument/{dokumentId}")
    public ResponseEntity<List<DokumentTagResponseDTO>> getByDokument(@PathVariable UUID dokumentId) {
        return ResponseEntity.ok(service.findByDokumentId(dokumentId));
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<List<DokumentTagResponseDTO>> getByTag(@PathVariable UUID tagId) {
        return ResponseEntity.ok(service.findByTagId(tagId));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DokumentTagResponseDTO>> bulkAssign(@Valid @RequestBody DokumentTagBulkRequestDTO request) {
        return ResponseEntity.ok(service.bulkAssign(request));
    }

    @DeleteMapping("/dokument/{dokumentId}")
    public ResponseEntity<Void> deleteByDokument(@PathVariable UUID dokumentId) {
        service.deleteByDokumentId(dokumentId);
        return ResponseEntity.ok().build();
    }
}
