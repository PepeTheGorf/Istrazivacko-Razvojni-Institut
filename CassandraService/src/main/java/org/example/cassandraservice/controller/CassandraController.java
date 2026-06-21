package org.example.cassandraservice.controller;

import org.example.cassandraservice.service.CassandraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

@RestController
@RequestMapping("/cassandra")
public class CassandraController {

    @Autowired
    private CassandraService cassandraService;

    @PostMapping("/llm-request")
    public UUID insertLlmRequest(@RequestParam String resId, @RequestParam String status) {
        return cassandraService.insertLlmRequest(resId, status);
    }

    @DeleteMapping("/llm-request/{resId}/{requestId}")
    public void deleteLlmRequest(@PathVariable String resId, @PathVariable UUID requestId) {
        cassandraService.deleteLlmRequestById(resId, requestId);
    }

    @PostMapping("/feedback")
    public UUID insertFeedback(@RequestParam String field, @RequestParam int rating) {
        return cassandraService.insertFeedback(field, rating);
    }

    @DeleteMapping("/feedback/{field}/{feedbackId}")
    public void deleteFeedback(@PathVariable String field, @PathVariable UUID feedbackId) {
        cassandraService.deleteFeedback(field, feedbackId);
    }

    @GetMapping("/llm-requests")
    public List<Map<String, Object>> getLlmRequestsByResearcher(@RequestParam String researcherId) {
        return cassandraService.getLlmRequestsByResearcher(researcherId);
    }

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
}