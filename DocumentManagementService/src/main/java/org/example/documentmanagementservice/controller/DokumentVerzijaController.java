package org.example.documentmanagementservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.model.Dokument;
import org.example.documentmanagementservice.model.DokumentVerzija;
import org.example.documentmanagementservice.model.NivoPrava;
import org.example.documentmanagementservice.service.DokumentVerzijaService;
import org.example.documentmanagementservice.service.PravaPristupaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dokumenti/{dokumentId}/verzije")
@RequiredArgsConstructor
public class DokumentVerzijaController {

    private final DokumentVerzijaService dokumentVerzijaService;
    private final PravaPristupaService pravaPristupaService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getHistory(
            @PathVariable UUID dokumentId,
            @RequestParam(required = false) String korisnikId) {

        checkReadAccess(parseUuid(korisnikId), dokumentId);

        List<DokumentVerzija> verzije = dokumentVerzijaService.getHistory(dokumentId);

        List<Map<String, Object>> result = verzije.stream().map(v -> {
            String preview = v.getSadrzaj() != null && v.getSadrzaj().length() > 200
                    ? v.getSadrzaj().substring(0, 200)
                    : v.getSadrzaj();
            return Map.<String, Object>of(
                    "id", v.getId(),
                    "verzijaBroj", v.getVerzijaBroj(),
                    "naslov", v.getNaslov(),
                    "sadrzajPreview", preview != null ? preview : "",
                    "sacuvaoId", v.getSacuvaoId() != null ? v.getSacuvaoId().toString() : "",
                    "datumKreiranja", v.getDatumKreiranja().toString()
            );
        }).toList();

        return ResponseEntity.ok(Map.of("verzije", result));
    }

    @GetMapping("/{verzijaId}")
    public ResponseEntity<Map<String, Object>> getOne(
            @PathVariable UUID dokumentId,
            @PathVariable UUID verzijaId,
            @RequestParam(required = false) String korisnikId) {

        checkReadAccess(parseUuid(korisnikId), dokumentId);

        DokumentVerzija v = dokumentVerzijaService.getHistory(dokumentId).stream()
                .filter(vz -> vz.getId().equals(verzijaId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verzija not found"));

        return ResponseEntity.ok(Map.of(
                "id", v.getId(),
                "verzijaBroj", v.getVerzijaBroj(),
                "naslov", v.getNaslov(),
                "sadrzaj", v.getSadrzaj() != null ? v.getSadrzaj() : "",
                "sacuvaoId", v.getSacuvaoId() != null ? v.getSacuvaoId().toString() : "",
                "datumKreiranja", v.getDatumKreiranja().toString()
        ));
    }

    @PostMapping("/{verzijaId}/restore")
    public ResponseEntity<Dokument> restore(
            @PathVariable UUID dokumentId,
            @PathVariable UUID verzijaId,
            @RequestBody Map<String, String> body) {

        UUID korisnikId = parseUuid(body.get("korisnikId"));
        checkWriteAccess(korisnikId, dokumentId);

        Dokument updated = dokumentVerzijaService.restore(dokumentId, verzijaId, korisnikId);
        return ResponseEntity.ok(updated);
    }

    private UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void checkReadAccess(UUID korisnikId, UUID dokumentId) {
        if (korisnikId == null) return;
        NivoPrava nivo = pravaPristupaService.checkAccess(korisnikId, dokumentId);
        if (nivo == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private void checkWriteAccess(UUID korisnikId, UUID dokumentId) {
        if (korisnikId == null) return;
        NivoPrava nivo = pravaPristupaService.checkAccess(korisnikId, dokumentId);
        if (nivo == null || nivo != NivoPrava.IZMENA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "IZMENA access required");
        }
    }
}
