package org.example.documentmanagementservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.documentmanagementservice.dto.AutoTagRequestDTO;
import org.example.documentmanagementservice.dto.AutoTagResponseDTO;
import org.example.documentmanagementservice.dto.SearchDocumentsRequestDTO;
import org.example.documentmanagementservice.dto.SearchDocumentsResponseDTO;
import org.example.documentmanagementservice.model.DokumentTag;
import org.example.documentmanagementservice.model.DokumentTagId;
import org.example.documentmanagementservice.model.Tag;
import org.example.documentmanagementservice.repository.DokumentRepository;
import org.example.documentmanagementservice.repository.DokumentTagRepository;
import org.example.documentmanagementservice.repository.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class AutoTagService {

    private final TagRepository tagRepository;
    private final DokumentTagRepository dokumentTagRepository;
    private final DokumentRepository dokumentRepository;
    private final RestTemplate restTemplate;

    @Value("${vector.search.url:http://vector-database-service:8000/api/v1/search/documents}")
    private String vectorSearchUrl;

    public AutoTagService(TagRepository tagRepository, DokumentTagRepository dokumentTagRepository, DokumentRepository dokumentRepository) {
        this.tagRepository = tagRepository;
        this.dokumentTagRepository = dokumentTagRepository;
        this.dokumentRepository = dokumentRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    public SearchDocumentsResponseDTO searchDocuments(SearchDocumentsRequestDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "query", request.getPrompt(),
                "top_k", 100,
                "threshold", request.getSimilarityThreshold()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> response;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = restTemplate.postForObject(vectorSearchUrl, entity, Map.class);
            response = raw;
        } catch (Exception ex) {
            log.error("Vector search service unavailable at {}: {}", vectorSearchUrl, ex.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "Servis za pretragu trenutno nije dostupan. Proverite da li je vector-database-service pokrenut."
            );
        }

        List<UUID> documentIds = new ArrayList<>();
        if (response != null && response.get("results") instanceof List<?> results) {
            for (Object item : results) {
                if (item instanceof Map<?, ?> resultMap) {
                    Object docId = resultMap.get("document_id");
                    if (docId != null) {
                        String milvusId = docId.toString();
                        dokumentRepository.findByVectorDocumentId(milvusId)
                                .ifPresentOrElse(
                                        doc -> documentIds.add(doc.getId()),
                                        () -> log.warn("No Postgres document found for Milvus id: {}", milvusId)
                                );
                    }
                }
            }
        }

        return SearchDocumentsResponseDTO.builder()
                .documentIds(documentIds)
                .count(documentIds.size())
                .suggestedTagName(request.getPrompt())
                .build();
    }

    @CacheEvict(value = "tag", allEntries = true)
    public AutoTagResponseDTO autoTag(AutoTagRequestDTO request) {
        List<String> appliedTags = new ArrayList<>();
        List<String> createdNewTags = new ArrayList<>();

        List<Tag> resolvedTags = new ArrayList<>();
        for (String tagName : request.getTagNames()) {
            String normalized = tagName.trim();
            if (normalized.isEmpty()) continue;

            Tag tag = tagRepository.findByNazivIgnoreCase(normalized).orElse(null);
            if (tag == null) {
                tag = tagRepository.save(Tag.builder().naziv(normalized).build());
                createdNewTags.add(normalized);
            }
            resolvedTags.add(tag);
            appliedTags.add(tag.getNaziv());
        }

        for (UUID dokumentId : request.getDocumentIds()) {
            for (Tag tag : resolvedTags) {
                DokumentTagId id = new DokumentTagId(dokumentId, tag.getId());
                if (!dokumentTagRepository.existsById(id)) {
                    dokumentTagRepository.save(
                            DokumentTag.builder()
                                    .dokumentId(dokumentId)
                                    .tagId(tag.getId())
                                    .build()
                    );
                }
            }
        }

        return AutoTagResponseDTO.builder()
                .appliedTags(appliedTags)
                .taggedDocumentCount(request.getDocumentIds().size())
                .createdNewTags(createdNewTags)
                .build();
    }
}
