package org.example.projectrealizationservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CassandraClient {

    private final RestTemplate restTemplate;

    @Value("${services.cassandra.url:http://cassandra-service:8080}")
    private String baseUrl;

    public UUID insertLlmRequest(String resId, String status) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/cassandra/llm-request")
                .queryParam("resId", resId)
                .queryParam("status", status)
                .toUriString();
        return restTemplate.postForObject(url, null, UUID.class);
    }

    public void deleteLlmRequest(String resId, UUID requestId) {
        String url = baseUrl + "/api/cassandra/llm-request/" + resId + "/" + requestId;
        restTemplate.delete(url);
    }

    public UUID insertFeedback(String field, int rating) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/cassandra/feedback")
                .queryParam("field", field)
                .queryParam("rating", rating)
                .toUriString();
        return restTemplate.postForObject(url, null, UUID.class);
    }

    public void deleteFeedback(String field, UUID feedbackId) {
        String url = baseUrl + "/api/cassandra/feedback/" + field + "/" + feedbackId;
        restTemplate.delete(url);
    }

    public List<Float> getTemplateEffectiveness(String templateId) {
        String url = baseUrl + "/api/cassandra/analytics/template-effectiveness/" + templateId;
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Float>>() {}).getBody();
    }

    public Map<String, Long> getRequestsCount() {
        String url = baseUrl + "/api/cassandra/analytics/requests-count";
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Long>>() {}).getBody();
    }

    public Map<String, Double> getAvgRating() {
        String url = baseUrl + "/api/cassandra/analytics/avg-rating";
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Double>>() {}).getBody();
    }
}
