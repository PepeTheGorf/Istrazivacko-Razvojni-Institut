package org.example.cassandraservice.controller;

import org.example.cassandraservice.model.*;
import org.example.cassandraservice.service.CassandraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cassandra")
public class CassandraController {

    @Autowired
    private CassandraService cassandraService;

    // ============= CRUD: LLM Requests =============

    @PostMapping("/llm-requests")
    public String createLlmRequest(@RequestBody LlmRequestByResearcher request) {
        return cassandraService.createLlmRequest(request);
    }

    @GetMapping("/llm-requests/{researcherId}")
    public List<LlmRequestByResearcher> getLlmRequests(@PathVariable String researcherId) {
        return cassandraService.getLlmRequestsByResearcher(researcherId);
    }

    @PutMapping("/llm-requests/{researcherId}")
    public String updateLlmRequestStatus(
            @PathVariable String researcherId,
            @RequestParam Instant timestamp,
            @RequestParam UUID requestId,
            @RequestParam String newStatus) {
        return cassandraService.updateLlmRequestStatus(researcherId, timestamp, requestId, newStatus);
    }

    @DeleteMapping("/llm-requests/{researcherId}")
    public String deleteLlmRequest(
            @PathVariable String researcherId,
            @RequestParam Instant timestamp,
            @RequestParam UUID requestId) {
        return cassandraService.deleteLlmRequest(researcherId, timestamp, requestId);
    }

    // ============= CRUD: Feedbacks =============

    @PostMapping("/feedbacks")
    public String createFeedback(@RequestBody FeedbackByManager feedback) {
        return cassandraService.createFeedback(feedback);
    }

    @GetMapping("/feedbacks/{managerId}")
    public List<FeedbackByManager> getFeedbacks(@PathVariable String managerId) {
        return cassandraService.getFeedbacksByManager(managerId);
    }

    @PutMapping("/feedbacks/{managerId}")
    public String updateFeedbackRating(
            @PathVariable String managerId,
            @RequestParam Instant feedbackDate,
            @RequestParam UUID feedbackId,
            @RequestParam int newRating) {
        return cassandraService.updateFeedbackRating(managerId, feedbackDate, feedbackId, newRating);
    }

    @DeleteMapping("/feedbacks/{managerId}")
    public String deleteFeedback(
            @PathVariable String managerId,
            @RequestParam Instant feedbackDate,
            @RequestParam UUID feedbackId) {
        return cassandraService.deleteFeedback(managerId, feedbackDate, feedbackId);
    }

    // ============= COMPLEX QUERIES =============

    /**
     * QUERY 1: Count requests by researcher with status filter
     * Example: GET /api/cassandra/analytics/requests-by-researcher?status=SUCCESS
     */
    @GetMapping("/analytics/requests-by-researcher")
    public Map<String, Long> getRequestCountByResearcher(@RequestParam(defaultValue = "SUCCESS") String status) {
        return cassandraService.complexQuery1_CountRequestsByResearcherWithStatusFilter(status);
    }

    /**
     * QUERY 2: Average rating by research field
     * Example: GET /api/cassandra/analytics/avg-rating-by-field
     */
    @GetMapping("/analytics/avg-rating-by-field")
    public Map<String, Double> getAverageRatingByField() {
        return cassandraService.complexQuery2_AverageRatingByResearchField();
    }

    /**
     * QUERY 3: Count regenerations per section
     * Example: GET /api/cassandra/analytics/regeneration-count
     */
    @GetMapping("/analytics/regeneration-count")
    public Map<String, Long> getRegenerationCount() {
        return cassandraService.complexQuery3_CountRegenerationsBySection();
    }

    /**
     * QUERY 4: Documents by date with status filter
     * Example: GET /api/cassandra/analytics/documents-by-date?date=2026-05-26&status=PUBLISHED
     */
    @GetMapping("/analytics/documents-by-date")
    public List<DocumentStatusByDate> getDocumentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "DRAFT") String status) {
        return cassandraService.complexQuery4_GetDocumentsByDateWithStatus(date, status);
    }

    /**
     * QUERY 5: Average effectiveness by prompt template
     * Example: GET /api/cassandra/analytics/prompt-effectiveness
     */
    @GetMapping("/analytics/prompt-effectiveness")
    public Map<String, Double> getPromptEffectiveness() {
        return cassandraService.complexQuery5_AverageEffectivenessByPromptTemplate();
    }
}
