package org.example.documentmanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.PravaPristupaRequestDTO;
import org.example.documentmanagementservice.dto.PravaPristupaResponseDTO;
import org.example.documentmanagementservice.service.PravaPristupaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prava-pristupa")
@RequiredArgsConstructor
public class PravaPristupaController {

    private final PravaPristupaService service;

    @GetMapping
    public ResponseEntity<List<PravaPristupaResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PravaPristupaResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PravaPristupaResponseDTO> create(@Valid @RequestBody PravaPristupaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PravaPristupaResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody PravaPristupaRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/korisnik/{korisnikId}")
    public ResponseEntity<List<PravaPristupaResponseDTO>> getByKorisnik(@PathVariable UUID korisnikId) {
        return ResponseEntity.ok(service.findByKorisnikId(korisnikId));
    }

    @GetMapping("/dokument/{dokumentId}")
    public ResponseEntity<List<PravaPristupaResponseDTO>> getByDokument(@PathVariable UUID dokumentId) {
        return ResponseEntity.ok(service.findByDokumentId(dokumentId));
    }

    @GetMapping("/projekat/{projekatId}")
    public ResponseEntity<List<PravaPristupaResponseDTO>> getByProjekat(@PathVariable UUID projekatId) {
        return ResponseEntity.ok(service.findByProjekatId(projekatId));
    }

    @GetMapping("/korisnik/{korisnikId}/dokument/{dokumentId}")
    public ResponseEntity<PravaPristupaResponseDTO> getByKorisnikAndDokument(@PathVariable UUID korisnikId, @PathVariable UUID dokumentId) {
        return ResponseEntity.ok(service.findByKorisnikAndDokument(korisnikId, dokumentId));
    }
}
