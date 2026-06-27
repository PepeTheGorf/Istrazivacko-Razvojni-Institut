package org.example.documentmanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.PravaPristupaRequestDTO;
import org.example.documentmanagementservice.dto.PravaPristupaResponseDTO;
import org.example.documentmanagementservice.model.NivoPrava;
import org.example.documentmanagementservice.service.PravaPristupaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<List<PravaPristupaResponseDTO>> getByProjekat(@PathVariable String projekatId) {
        return ResponseEntity.ok(service.findByProjekatId(toProjekatUuid(projekatId)));
    }

    @GetMapping("/korisnik/{korisnikId}/dokument/{dokumentId}")
    public ResponseEntity<PravaPristupaResponseDTO> getByKorisnikAndDokument(@PathVariable UUID korisnikId, @PathVariable UUID dokumentId) {
        return ResponseEntity.ok(service.findByKorisnikAndDokument(korisnikId, dokumentId));
    }

    @PostMapping("/grant/dokument")
    public ResponseEntity<PravaPristupaResponseDTO> grantDokumentAccess(@RequestBody GrantRequestBody body) {
        return ResponseEntity.ok(service.grantDokumentAccess(
                body.dokumentId(), toKorisnikUuid(body.korisnikId()), body.nivo(), toKorisnikUuid(body.dodeljivaoId())));
    }

    @PostMapping("/grant/projekat")
    public ResponseEntity<PravaPristupaResponseDTO> grantProjekatAccess(@RequestBody GrantProjekatRequestBody body) {
        return ResponseEntity.ok(service.grantProjekatAccess(
                toProjekatUuid(body.projekatId()), toKorisnikUuid(body.korisnikId()), body.nivo(), toKorisnikUuid(body.dodeljivaoId())));
    }

    @DeleteMapping("/revoke/dokument")
    public ResponseEntity<Void> revokeDokumentAccess(@RequestBody RevokeRequestBody body) {
        service.revokeDokumentAccess(body.dokumentId(), toKorisnikUuid(body.korisnikId()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/revoke/projekat")
    public ResponseEntity<Void> revokeProjekatAccess(@RequestBody RevokeProjekatRequestBody body) {
        service.revokeProjekatAccess(toProjekatUuid(body.projekatId()), toKorisnikUuid(body.korisnikId()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, String>> checkAccess(
            @RequestParam String korisnikId,
            @RequestParam UUID dokumentId) {
        NivoPrava nivo = service.checkAccess(toKorisnikUuid(korisnikId), dokumentId);
        return ResponseEntity.ok(Map.of("nivo", nivo != null ? nivo.name() : "null"));
    }

    @GetMapping("/moji")
    public ResponseEntity<List<PravaPristupaResponseDTO>> getMojiPristup(@RequestParam String korisnikId) {
        return ResponseEntity.ok(service.findByKorisnikId(toKorisnikUuid(korisnikId)));
    }

    @GetMapping("/projekti-pristup")
    public ResponseEntity<List<String>> getProjekatPristupIds(
            @RequestParam String korisnikId,
            @RequestParam List<String> projekatIds) {
        UUID korisnikUuid = toKorisnikUuid(korisnikId);
        List<String> allowed = projekatIds.stream()
                .filter(pid -> service.hasProjekatAccess(korisnikUuid, toProjekatUuid(pid)))
                .toList();
        return ResponseEntity.ok(allowed);
    }

    private static UUID toProjekatUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(("project:" + id).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private static UUID toKorisnikUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            if (id.matches("\\d+")) {
                return UUID.nameUUIDFromBytes(("stakeholder:" + id).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            throw new IllegalArgumentException("Invalid korisnikId: " + id);
        }
    }

    record GrantRequestBody(UUID dokumentId, String korisnikId, NivoPrava nivo, String dodeljivaoId) {}
    record GrantProjekatRequestBody(String projekatId, String korisnikId, NivoPrava nivo, String dodeljivaoId) {}
    record RevokeRequestBody(UUID dokumentId, String korisnikId) {}
    record RevokeProjekatRequestBody(String projekatId, String korisnikId) {}
}
