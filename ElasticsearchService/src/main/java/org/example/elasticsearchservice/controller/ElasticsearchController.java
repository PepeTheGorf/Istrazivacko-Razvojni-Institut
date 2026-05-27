package org.example.elasticsearchservice.controller;

import org.example.elasticsearchservice.model.GeneratedSection;
import org.example.elasticsearchservice.model.ManagerPrompt;
import org.example.elasticsearchservice.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/elasticsearch")
public class ElasticsearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    // ============= CRUD: GeneratedSections =============

    @PostMapping("/sections")
    public String createSection(@RequestBody GeneratedSection section) {
        return elasticsearchService.createGeneratedSection(section);
    }

    @GetMapping("/sections/{id}")
    public GeneratedSection getSection(@PathVariable String id) {
        return elasticsearchService.getGeneratedSection(id);
    }

    @PutMapping("/sections/{id}")
    public String updateSection(@PathVariable String id, @RequestBody GeneratedSection section) {
        return elasticsearchService.updateGeneratedSection(id, section);
    }

    @DeleteMapping("/sections/{id}")
    public String deleteSection(@PathVariable String id) {
        return elasticsearchService.deleteGeneratedSection(id);
    }

    // ============= CRUD: ManagerPrompts =============

    @PostMapping("/prompts")
    public String createPrompt(@RequestBody ManagerPrompt prompt) {
        return elasticsearchService.createManagerPrompt(prompt);
    }

    @GetMapping("/prompts/{id}")
    public ManagerPrompt getPrompt(@PathVariable String id) {
        return elasticsearchService.getManagerPrompt(id);
    }

    @PutMapping("/prompts/{id}")
    public String updatePrompt(@PathVariable String id, @RequestBody ManagerPrompt prompt) {
        return elasticsearchService.updateManagerPrompt(id, prompt);
    }

    @DeleteMapping("/prompts/{id}")
    public String deletePrompt(@PathVariable String id) {
        return elasticsearchService.deleteManagerPrompt(id);
    }

    // ============= COMPLEX QUERIES =============

    /**
     * COMPLEX QUERY 1: Full-Text Search + Filter + Sort
     * Example: GET /api/elasticsearch/search/fulltext?text=research&status=APPROVED
     */
    @GetMapping("/search/fulltext")
    public List<GeneratedSection> searchFullTextWithFilter(
            @RequestParam String text,
            @RequestParam(defaultValue = "APPROVED") String status) {
        return elasticsearchService.complexQuery1_FullTextWithFilterAndSort(text, status);
    }

    /**
     * COMPLEX QUERY 2: Multiple Conditions + Term Aggregation
     * Example: GET /api/elasticsearch/search/category-stats?minEffectiveness=0.7
     */
    @GetMapping("/search/category-stats")
    public Map<String, Long> searchCategoryStats(
            @RequestParam(defaultValue = "0.0") float minEffectiveness) {
        return elasticsearchService.complexQuery2_MultipleConditionsWithTermAggregation(minEffectiveness);
    }

    /**
     * COMPLEX QUERY 3: Text Search + Date Range + Sub-Aggregation
     * Example: GET /api/elasticsearch/search/date-range?text=analysis&startDate=1700000000000&endDate=1800000000000
     */
    @GetMapping("/search/date-range")
    public Map<String, Double> searchWithDateRangeAggregation(
            @RequestParam String text,
            @RequestParam long startDate,
            @RequestParam long endDate) {
        return elasticsearchService.complexQuery3_TextSearchWithDateRangeAndSubAggregation(text, startDate, endDate);
    }
}
