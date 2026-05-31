package org.example.documentmanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.MetapodatakRequestDTO;
import org.example.documentmanagementservice.dto.MetapodatakResponseDTO;
import org.example.documentmanagementservice.service.MetapodatakService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/metapodatak")
@RequiredArgsConstructor
public class MetapodatakController {

    private final MetapodatakService service;

    @GetMapping
    public ResponseEntity<List<MetapodatakResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetapodatakResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<MetapodatakResponseDTO> create(@Valid @RequestBody MetapodatakRequestDTO request) {
        MetapodatakResponseDTO created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetapodatakResponseDTO> update(@PathVariable UUID id,
                                                         @Valid @RequestBody MetapodatakRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dokument/{dokumentId}")
    public ResponseEntity<List<MetapodatakResponseDTO>> getByDokument(@PathVariable UUID dokumentId) {
        return ResponseEntity.ok(service.findByDokumentId(dokumentId));
    }
}
