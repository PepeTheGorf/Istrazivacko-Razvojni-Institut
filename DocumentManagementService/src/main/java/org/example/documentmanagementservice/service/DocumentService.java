package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.documentmanagementservice.dto.DokumentRequestDTO;
import org.example.documentmanagementservice.dto.MetapodatakCreateDTO;
import org.example.documentmanagementservice.model.Dokument;
import org.example.documentmanagementservice.model.Tag;
import org.example.documentmanagementservice.repository.DokumentRepository;
import org.example.documentmanagementservice.repository.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DokumentRepository dokumentRepository;
    private final TagService tagService;
    private final DokumentTagService dokumentTagService;
    private final MetapodatakService metapodatakService;
    private final TagRepository tagRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vector.service.url:http://vector-database-service:8000/api/v1/documents}")
    private String vectorServiceUrl;

    @Value("${project.service.url:http://project-realization-service:8080/projects}")
    private String projectServiceUrl;

    @Transactional
    public Dokument create(DokumentRequestDTO request) {
        UUID resolvedProjectId = resolveProjectId(request.getProjektId());

        // enforce project existence if provided
        if (request.getProjektId() != null && !request.getProjektId().isBlank() && !projectExists(request.getProjektId().trim())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Project not found: " + request.getProjektId());
        }
        UUID id = UUID.randomUUID();
        Dokument dokument = Dokument.builder()
                .id(id)
                .naslov(request.getNaslov())
                .sadrzaj(request.getSadrzaj())
                .authorId(resolveAuthorId(request.getAuthorId()))
            .authorName(normalizeOptionalText(request.getAuthorName()))
                .projektId(resolvedProjectId)
            .projectName(normalizeOptionalText(request.getProjectName()))
                .tipDokumentaId(request.getTipDokumentaId())
                .createdAt(Instant.now())
                .build();

        dokument = dokumentRepository.save(dokument);

        // tags: create or find by name, then assign
        if (request.getTagovi() != null && !request.getTagovi().isEmpty()) {
            List<UUID> tagIds = new ArrayList<>();
            for (String naziv : request.getTagovi()) {
                var existing = tagRepository.findByNaziv(naziv.trim()).orElse(null);
                Tag tag;
                if (existing == null) {
                    tag = Tag.builder().naziv(naziv.trim()).build();
                    tag = tagRepository.save(tag);
                } else {
                    tag = existing;
                }
                tagIds.add(tag.getId());
                dokumentTagService.create(new org.example.documentmanagementservice.dto.DokumentTagRequestDTO(dokument.getId(), tag.getId()));
            }
        }

        // metadata
        if (request.getMetapodaci() != null && !request.getMetapodaci().isEmpty()) {
            for (MetapodatakCreateDTO m : request.getMetapodaci()) {
                metapodatakService.create(new org.example.documentmanagementservice.dto.MetapodatakRequestDTO(dokument.getId(), m.getTipMetapodatkaId(), m.getVrednost()));
            }
        }

        // Call vector service synchronously and persist returned vector id when available
        try {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("title", dokument.getNaslov());
            payload.put("author_id", dokument.getAuthorId().toString());
            payload.put("content", dokument.getSadrzaj());
            payload.put("project_id", request.getProjektId() == null || request.getProjektId().isBlank() ? null : request.getProjektId().trim());
            payload.put("doc_type_id", dokument.getTipDokumentaId() == null ? null : dokument.getTipDokumentaId().toString());

            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.postForObject(vectorServiceUrl, new HttpEntity<>(payload, jsonHeaders()), Map.class);

            if (resp != null && resp.containsKey("inserted_ids")) {
                Object idsObj = resp.get("inserted_ids");
                if (idsObj instanceof List<?> idsList && !idsList.isEmpty()) {
                    Object first = idsList.get(0);
                    // Save whatever the vector service returned as a String id
                    dokument.setVectorDocumentId(first == null ? null : first.toString());
                    dokument = dokumentRepository.save(dokument);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to index document in vector service - continuing without blocking create", ex);
        }

        return dokument;
    }

    @Transactional
    public Dokument update(java.util.UUID id, DokumentRequestDTO request) {
        UUID resolvedProjectId = resolveProjectId(request.getProjektId());

        // enforce project existence if provided
        if (request.getProjektId() != null && !request.getProjektId().isBlank() && !projectExists(request.getProjektId().trim())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Project not found: " + request.getProjektId());
        }

        var existing = dokumentRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Dokument not found"));
        existing.setNaslov(request.getNaslov());
        existing.setSadrzaj(request.getSadrzaj());
        existing.setAuthorId(resolveAuthorId(request.getAuthorId()));
        existing.setAuthorName(normalizeOptionalText(request.getAuthorName()));
        existing.setProjektId(resolvedProjectId);
        existing.setProjectName(normalizeOptionalText(request.getProjectName()));
        existing.setTipDokumentaId(request.getTipDokumentaId());

        existing = dokumentRepository.save(existing);

        // propagate to vector service: if we already have vector id, PUT; otherwise POST and persist returned id
        try {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("title", existing.getNaslov());
            payload.put("author_id", existing.getAuthorId().toString());
            payload.put("content", existing.getSadrzaj());
            payload.put("project_id", request.getProjektId() == null || request.getProjektId().isBlank() ? null : request.getProjektId().trim());
            payload.put("doc_type_id", existing.getTipDokumentaId() == null ? null : existing.getTipDokumentaId().toString());

            if (existing.getVectorDocumentId() != null) {
                String url = vectorServiceUrl + "/" + existing.getVectorDocumentId();
                restTemplate.put(url, new HttpEntity<>(payload, jsonHeaders()));
            } else {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> resp = restTemplate.postForObject(vectorServiceUrl, new HttpEntity<>(payload, jsonHeaders()), java.util.Map.class);
                if (resp != null && resp.containsKey("inserted_ids")) {
                    Object idsObj = resp.get("inserted_ids");
                    if (idsObj instanceof java.util.List<?> idsList && !idsList.isEmpty()) {
                        Object first = idsList.get(0);
                        existing.setVectorDocumentId(first == null ? null : first.toString());
                        existing = dokumentRepository.save(existing);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to propagate update to vector service - continuing", ex);
        }

        return existing;
    }

    private boolean projectExists(String projectId) {
        try {
            String url = projectServiceUrl + "/" + projectId;
            restTemplate.getForObject(url, java.util.Map.class);
            return true;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
            return false;
        } catch (Exception ex) {
            // if project service is unreachable, fail fast so client knows
            log.error("Failed to verify project existence at {}", projectServiceUrl, ex);
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Project service unavailable");
        }
    }

    @Transactional
    public void delete(java.util.UUID id) {
        var existing = dokumentRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Dokument not found"));

        // attempt to delete from vector service if indexed
        if (existing.getVectorDocumentId() != null) {
            try {
                String url = vectorServiceUrl + "/" + existing.getVectorDocumentId();
                restTemplate.delete(url);
            } catch (Exception ex) {
                log.error("Failed to delete document from vector service - continuing", ex);
            }
        }

        dokumentRepository.delete(existing);
    }

    public Dokument getById(java.util.UUID id) {
        return dokumentRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Dokument not found"));
    }

    public java.util.List<Dokument> listAll() {
        return dokumentRepository.findAll();
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private UUID resolveAuthorId(String rawAuthorId) {
        if (rawAuthorId == null || rawAuthorId.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Author id is required"
            );
        }

        String normalized = rawAuthorId.trim();
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException ignored) {
            // Stakeholder service currently returns numeric IDs, so map them to deterministic UUID values.
            if (normalized.matches("\\d+")) {
                return UUID.nameUUIDFromBytes(("stakeholder:" + normalized).getBytes(StandardCharsets.UTF_8));
            }
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Author id must be a UUID or numeric stakeholder id"
            );
        }
    }

    private UUID resolveProjectId(String rawProjectId) {
        if (rawProjectId == null || rawProjectId.isBlank()) {
            return null;
        }

        String normalized = rawProjectId.trim();
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException ignored) {
            // Project service currently returns numeric IDs, so map them to deterministic UUID values.
            if (normalized.matches("\\d+")) {
                return UUID.nameUUIDFromBytes(("project:" + normalized).getBytes(StandardCharsets.UTF_8));
            }
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Project id must be a UUID or numeric project id"
            );
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
