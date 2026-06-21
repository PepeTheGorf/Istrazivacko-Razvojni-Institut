package org.example.projectrealizationservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorDatabaseClient {

    private final RestTemplate restTemplate;

    @Value("${services.vector-database.url:http://vector-database-service:8000}")
    private String baseUrl;

    public Long createDocument(String title, String authorId, String content, List<String> tags, Map<String, String> metadata) {
        String url = baseUrl + "/api/v1/documents";
        Map<String, Object> body = Map.of(
            "title", title,
            "author_id", authorId,
            "doc_type_id", "generated",
            "project_id", "",
            "content", content,
            "tags", tags,
            "metadata", metadata
        );
        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("inserted_ids") == null) {
            throw new RuntimeException("VectorDatabaseService nije vratio ID dokumenta");
        }
        List<?> ids = (List<?>) responseBody.get("inserted_ids");
        return ((Number) ids.get(0)).longValue();
    }

    public Long createChunk(Long documentId, int chunkIndex, String sectionTitle, String chunkText) {
        String url = baseUrl + "/api/v1/chunks";
        Map<String, Object> body = Map.of(
            "document_id", documentId,
            "chunk_index", chunkIndex,
            "section_title", sectionTitle,
            "chunk_text", chunkText,
            "source_page", 0
        );
        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("inserted_ids") == null) {
            throw new RuntimeException("VectorDatabaseService nije vratio ID chunka");
        }
        List<?> ids = (List<?>) responseBody.get("inserted_ids");
        return ((Number) ids.get(0)).longValue();
    }

    public void deleteDocument(Long documentId) {
        String url = baseUrl + "/api/v1/documents/" + documentId;
        restTemplate.delete(url);
    }

    public void deleteChunk(Long chunkId) {
        String url = baseUrl + "/api/v1/chunks/" + chunkId;
        restTemplate.delete(url);
    }
}
