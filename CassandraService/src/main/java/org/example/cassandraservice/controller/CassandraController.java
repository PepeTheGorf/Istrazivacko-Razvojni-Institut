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
}