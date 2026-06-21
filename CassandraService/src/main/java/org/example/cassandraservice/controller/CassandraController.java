package org.example.cassandraservice.controller;

import org.example.cassandraservice.service.CassandraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/cassandra")
public class CassandraController {

    @Autowired
    private CassandraService cassandraService;

    //analytics endpoints

    @GetMapping("/analytics/requests-count")
    public Map<String, Long> getCountByResearcher() {
        return cassandraService.query1_CountRequestsByResearcher();
    }

    @GetMapping("/analytics/avg-rating")
    public Map<String, Double> getAvgRating() {
        return cassandraService.query2_AvgRatingByField();
    }

    @GetMapping("/analytics/regen-count")
    public Map<String, Long> getRegenCount() {
        return cassandraService.query3_CountRegensBySection();
    }

    @GetMapping("/analytics/docs-by-status")
    public List<String> getDocsByStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String status) {
        return cassandraService.query4_GetDocsByDateAndStatus(date, status);
    }

    @GetMapping("/analytics/template-effectiveness/{id}")
    public List<Float> getTemplateEffectiveness(@PathVariable String id) {
        return cassandraService.query5_GetEffectivenessByTemplate(id);
    }

    @PostMapping("/llm-request")
    public void insertLlmRequest(@RequestParam String resId, @RequestParam String status){
        cassandraService.insertLlmRequest(resId, java.time.Instant.now(), UUID.randomUUID(), status);
    }
    @PostMapping("/feedback")
    public void insertFeedback(@RequestParam String field,  @RequestParam int rating){
        cassandraService.insertFeedback(field, UUID.randomUUID(), rating);
    }
    @DeleteMapping("/llm-request/{resId}/{timestamp}/{requestId}")
    public void deleteLlmRequest(@PathVariable String resId, @PathVariable String timestamp, @PathVariable java.util.UUID requestId) {
        cassandraService.deleteLlmRequest(resId, java.time.Instant.parse(timestamp), requestId);
    }
}