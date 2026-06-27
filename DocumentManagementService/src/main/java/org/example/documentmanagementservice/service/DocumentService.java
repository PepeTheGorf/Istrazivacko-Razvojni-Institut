package org.example.documentmanagementservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.example.documentmanagementservice.dto.DokumentRequestDTO;
import org.example.documentmanagementservice.dto.MetapodatakCreateDTO;
import org.example.documentmanagementservice.model.Dokument;
import org.example.documentmanagementservice.model.Tag;
import org.example.documentmanagementservice.repository.DokumentRepository;
import org.example.documentmanagementservice.repository.MetapodatakRepository;
import org.example.documentmanagementservice.repository.TagRepository;
import org.example.documentmanagementservice.service.DokumentVerzijaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DokumentRepository dokumentRepository;
    private final TagService tagService;
    private final DokumentTagService dokumentTagService;
    private final MetapodatakService metapodatakService;
    private final MetapodatakRepository metapodatakRepository;
    private final TagRepository tagRepository;
    private final PravaPristupaService pravaPristupaService;
    private final DokumentVerzijaService dokumentVerzijaService;

    private final RestTemplate restTemplate = createRestTemplate();

    @Value("${vector.service.url:http://vector-database-service:8000/api/v1/documents}")
    private String vectorServiceUrl;

    @Value("${project.service.url:http://project-realization-service:8080/projects}")
    private String projectServiceUrl;

    @Value("${analytics.service.url:http://document-acsses-service:8082/document-access.json/save}")
    private String analyticsServiceUrl;

    @Transactional
    @CacheEvict(value = "tag", allEntries = true)
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
            String rawContent = dokument.getSadrzaj() != null ? dokument.getSadrzaj() : "";
            String truncatedContent = rawContent.length() > 65000 ? rawContent.substring(0, 65000) : rawContent;
            payload.put("title", dokument.getNaslov());
            payload.put("author_id", dokument.getAuthorId().toString());
            payload.put("content", truncatedContent);
            payload.put("project_id", request.getProjektId() == null || request.getProjektId().isBlank() ? "" : request.getProjektId().trim());
            payload.put("doc_type_id", dokument.getTipDokumentaId() == null ? "" : dokument.getTipDokumentaId().toString());

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
    @CacheEvict(value = "tag", allEntries = true)
    public Dokument update(java.util.UUID id, DokumentRequestDTO request) {
        UUID resolvedProjectId = resolveProjectId(request.getProjektId());

        // enforce project existence if provided
        if (request.getProjektId() != null && !request.getProjektId().isBlank() && !projectExists(request.getProjektId().trim())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Project not found: " + request.getProjektId());
        }

        var existing = dokumentRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Dokument not found"));

        UUID requestingUserId = resolveAuthorId(request.getAuthorId());
        dokumentVerzijaService.createVerzija(existing, requestingUserId);

        existing.setNaslov(request.getNaslov());
        existing.setSadrzaj(request.getSadrzaj());
        existing.setAuthorId(resolveAuthorId(request.getAuthorId()));
        existing.setAuthorName(normalizeOptionalText(request.getAuthorName()));
        existing.setProjektId(resolvedProjectId);
        existing.setProjectName(normalizeOptionalText(request.getProjectName()));
        existing.setTipDokumentaId(request.getTipDokumentaId());

        existing = dokumentRepository.save(existing);

        // replace all existing tag assignments with the submitted set
        dokumentTagService.deleteByDokumentId(existing.getId());
        if (request.getTagovi() != null && !request.getTagovi().isEmpty()) {
            for (String naziv : request.getTagovi()) {
                if (naziv == null || naziv.isBlank()) {
                    continue;
                }
                var existingTag = tagRepository.findByNaziv(naziv.trim()).orElse(null);
                Tag tag;
                if (existingTag == null) {
                    tag = Tag.builder().naziv(naziv.trim()).build();
                    tag = tagRepository.save(tag);
                } else {
                    tag = existingTag;
                }
                dokumentTagService.create(new org.example.documentmanagementservice.dto.DokumentTagRequestDTO(existing.getId(), tag.getId()));
            }
        }

        // replace all existing metadata values with the submitted set
        metapodatakService.deleteByDokumentId(existing.getId());
        if (request.getMetapodaci() != null && !request.getMetapodaci().isEmpty()) {
            for (MetapodatakCreateDTO m : request.getMetapodaci()) {
                if (m == null || m.getTipMetapodatkaId() == null || m.getVrednost() == null || m.getVrednost().isBlank()) {
                    continue;
                }
                metapodatakService.create(
                        new org.example.documentmanagementservice.dto.MetapodatakRequestDTO(
                                existing.getId(),
                                m.getTipMetapodatkaId(),
                                m.getVrednost()
                        )
                );
            }
        }

        // propagate to vector service: if we already have vector id, PUT; otherwise POST and persist returned id
        try {
            var payload = new java.util.HashMap<String, Object>();
            String rawContentUpdate = existing.getSadrzaj() != null ? existing.getSadrzaj() : "";
            String truncatedContentUpdate = rawContentUpdate.length() > 65000 ? rawContentUpdate.substring(0, 65000) : rawContentUpdate;
            payload.put("title", existing.getNaslov());
            payload.put("author_id", existing.getAuthorId().toString());
            payload.put("content", truncatedContentUpdate);
            payload.put("project_id", request.getProjektId() == null || request.getProjektId().isBlank() ? "" : request.getProjektId().trim());
            payload.put("doc_type_id", existing.getTipDokumentaId() == null ? "" : existing.getTipDokumentaId().toString());

            if (existing.getVectorDocumentId() != null) {
                String url = vectorServiceUrl + "/" + existing.getVectorDocumentId();
                ResponseEntity<Map> updateResponse = restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        new HttpEntity<>(payload, jsonHeaders()),
                        Map.class
                );
                Object newId = extractPrimaryId(updateResponse.getBody());
                if (newId != null) {
                    String newVectorId = newId.toString();
                    if (!newVectorId.equals(existing.getVectorDocumentId())) {
                        existing.setVectorDocumentId(newVectorId);
                        existing = dokumentRepository.save(existing);
                    }
                }
            } else {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> resp = restTemplate.postForObject(vectorServiceUrl, new HttpEntity<>(payload, jsonHeaders()), java.util.Map.class);
                if (resp != null && (resp.containsKey("ids") || resp.containsKey("inserted_ids"))) {
                    Object idsObj = resp.containsKey("ids") ? resp.get("ids") : resp.get("inserted_ids");
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
            restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(forwardAuthHeaders()), java.util.Map.class);
            return true;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
            return false;
        } catch (Exception ex) {
            log.error("Failed to verify project existence at {}", projectServiceUrl, ex);
            return true;
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
        Dokument dokument = dokumentRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Dokument not found"));
        trackDocumentAccess(dokument);
        return dokument;
    }

    private void trackDocumentAccess(Dokument dokument) {
        String userId = extractCurrentUserId();
        String documentId = dokument.getId().toString();
        String projectId = dokument.getProjektId() != null ? dokument.getProjektId().toString() : "";
        long fileSizeBytes = dokument.getSadrzaj() != null ? dokument.getSadrzaj().getBytes(StandardCharsets.UTF_8).length : 0L;

        // Build headers in the request thread before handing off — RequestContextHolder is not available in new threads
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        new Thread(() -> {
            try {
                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("user_id", userId);
                payload.put("document_id", documentId);
                payload.put("project_id", projectId);
                payload.put("action_type", "VIEW");
                payload.put("session_duration_sec", 0L);
                payload.put("file_size_bytes", fileSizeBytes);
                payload.put("created", Instant.now().toString());
                restTemplate.postForObject(analyticsServiceUrl, new HttpEntity<>(payload, headers), Object.class);
            } catch (Exception ex) {
                log.error("Failed to record document access analytics - continuing", ex);
            }
        }).start();
    }

    private String extractCurrentUserId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            String auth = servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.startsWith("Bearer ")) {
                try {
                    String payload = auth.substring(7);
                    String[] parts = payload.split("\\.");
                    if (parts.length >= 2) {
                        String decoded = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                        com.fasterxml.jackson.databind.JsonNode node = new ObjectMapper().readTree(decoded);
                        if (node.has("sub")) return node.get("sub").asText();
                        if (node.has("id")) return node.get("id").asText();
                        if (node.has("userId")) return node.get("userId").asText();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return "unknown";
    }

    public java.util.List<Dokument> listAll() {
        return dokumentRepository.findAll();
    }

    public java.util.List<Dokument> listByProjekat(String rawProjektId) {
        UUID projektUuid = resolveProjectId(rawProjektId);
        if (projektUuid == null) return java.util.List.of();
        return dokumentRepository.findByProjektId(projektUuid);
    }

    public java.util.List<Dokument> searchDokumenti(org.example.documentmanagementservice.dto.DokumentSearchRequestDTO req) {
        String naslov = blankToNull(req.getNaslov());
        String autor = blankToNull(req.getAutor());
        UUID tipDokumentaId = req.getTipDokumentaId();
        UUID projektId = resolveProjectId(req.getProjektId());
        String tag = blankToNull(req.getTag());

        Instant dateFrom = req.getDateFrom() != null
                ? req.getDateFrom().atStartOfDay(java.time.ZoneOffset.UTC).toInstant()
                : null;
        Instant dateTo = req.getDateTo() != null
                ? req.getDateTo().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()
                : null;

        var result = dokumentRepository.searchDokumenti(naslov, autor, tipDokumentaId, projektId, dateFrom, dateTo, tag);

        if (req.getMetadataFilters() != null && !req.getMetadataFilters().isEmpty()) {
            List<UUID> docIds = result.stream().map(Dokument::getId).toList();
            Map<UUID, List<org.example.documentmanagementservice.model.Metapodatak>> metaByDoc =
                    metapodatakRepository.findByDokumentIdIn(docIds).stream()
                            .collect(Collectors.groupingBy(org.example.documentmanagementservice.model.Metapodatak::getDokumentId));

            result = result.stream()
                    .filter(doc -> matchesAllMetadataFilters(metaByDoc.getOrDefault(doc.getId(), List.of()), req.getMetadataFilters()))
                    .toList();
        }

        return result;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private boolean matchesAllMetadataFilters(
            List<org.example.documentmanagementservice.model.Metapodatak> metas,
            List<org.example.documentmanagementservice.dto.DokumentSearchRequestDTO.MetadataFilterDTO> filters) {
        return filters.stream()
                .filter(f -> f.getTipMetapodatkaId() != null)
                .allMatch(f -> {
                    String filterVal = f.getVrednost() == null ? "" : f.getVrednost().trim().toLowerCase();
                    return metas.stream()
                            .filter(m -> f.getTipMetapodatkaId().equals(m.getTipMetapodatkaId()))
                            .anyMatch(m -> filterVal.isBlank() ||
                                    (m.getVrednost() != null && m.getVrednost().toLowerCase().contains(filterVal)));
                });
    }

    public java.util.List<Dokument> listForKorisnik(String rawKorisnikId) {
        UUID korisnikUuid = resolveKorisnikId(rawKorisnikId);
        return dokumentRepository.findAll().stream()
                .filter(doc -> pravaPristupaService.checkAccess(korisnikUuid, doc.getId()) != null)
                .toList();
    }

    private UUID resolveKorisnikId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "korisnikId is required");
        }
        String normalized = rawId.trim();
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException ignored) {
            if (normalized.matches("\\d+")) {
                return UUID.nameUUIDFromBytes(("stakeholder:" + normalized).getBytes(StandardCharsets.UTF_8));
            }
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "korisnikId must be a UUID or numeric id");
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.addAll(forwardAuthHeaders());
        return headers;
    }

    private HttpHeaders forwardAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            String authHeader = servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && !authHeader.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }
        return headers;
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Transactional
    public Dokument upload(MultipartFile file, String naziv, UUID tipDokumentaId, String projektId, String projectName, String authorId, String authorName, String metapodaciJson) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String resolvedNaziv = (naziv != null && !naziv.isBlank()) ? naziv.trim() : stripExtension(filename);

        String sadrzaj;
        try {
            sadrzaj = extractText(file);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to extract text from file: " + ex.getMessage());
        }

        List<MetapodatakCreateDTO> metapodaci = null;
        if (metapodaciJson != null && !metapodaciJson.isBlank()) {
            try {
                metapodaci = OBJECT_MAPPER.readValue(metapodaciJson, new TypeReference<>() {});
            } catch (IOException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid metapodaci JSON: " + ex.getMessage());
            }
        }

        DokumentRequestDTO request = new DokumentRequestDTO();
        request.setNaslov(resolvedNaziv);
        request.setAuthorId(authorId);
        request.setAuthorName(authorName);
        request.setSadrzaj(sadrzaj);
        request.setTipDokumentaId(tipDokumentaId);
        request.setProjektId(projektId);
        request.setProjectName(projectName);
        request.setMetapodaci(metapodaci);

        return create(request);
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try (InputStream in = file.getInputStream()) {
            if (filename.endsWith(".pdf")) {
                try (PDDocument pdf = Loader.loadPDF(in.readAllBytes())) {
                    return new PDFTextStripper().getText(pdf);
                }
            } else if (filename.endsWith(".docx")) {
                try (XWPFDocument docx = new XWPFDocument(in)) {
                    return docx.getParagraphs().stream()
                            .map(XWPFParagraph::getText)
                            .collect(Collectors.joining("\n"));
                }
            } else {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Object extractPrimaryId(Map<?, ?> body) {
        if (body == null) {
            return null;
        }

        Object singleId = body.get("id");
        if (singleId != null) {
            return singleId;
        }

        Object ids = body.get("ids");
        if (ids instanceof List<?> idsList && !idsList.isEmpty()) {
            return idsList.get(0);
        }

        return null;
    }
}
