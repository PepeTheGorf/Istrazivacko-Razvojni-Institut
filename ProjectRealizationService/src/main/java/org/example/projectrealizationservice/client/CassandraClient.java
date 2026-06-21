package org.example.projectrealizationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List; 
import java.util.Map;

@FeignClient(name = "cassandra-service")
public interface CassandraClient {

    @PostMapping("/api/cassandra/llm-request")
    void insertLlmRequest(@RequestParam("resId") String resId, 
                          @RequestParam("status") String status);

    @PostMapping("/api/cassandra/feedback")
    void insertFeedback(@RequestParam("field") String field, 
                        @RequestParam("rating") int rating);

    @GetMapping("/api/cassandra/analytics/requests-count")
    Map<String, Long> getRequestsCount();

    @GetMapping("/api/cassandra/analytics/avg-rating")
    Map<String, Double> getAvgRating();

    @GetMapping("/api/cassandra/analytics/template-effectiveness/{id}")
    List<Float> getTemplateEffectiveness(@PathVariable("id") String id);
}