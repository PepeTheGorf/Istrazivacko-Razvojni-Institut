package org.example.elasticsearchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.elasticsearchservice.model.GeneratedSection;
import org.example.elasticsearchservice.model.ManagerPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ElasticsearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    //private final ObjectMapper objectMapper = new ObjectMapper();

    // ============= CRUD for GeneratedSections =============

    public String createGeneratedSection(GeneratedSection section) {
        try {
            if (section.getId() == null) {
                section.setId(UUID.randomUUID().toString());
            }
            IndexRequest<GeneratedSection> request = new IndexRequest.Builder<GeneratedSection>()
                    .index(ElasticsearchInitializationService.GENERATED_SECTIONS_INDEX)
                    .id(section.getId())
                    .document(section)
                    .build();
            elasticsearchClient.index(request);
            return "Generated section created with ID: " + section.getId();
        } catch (Exception e) {
            return "Error creating section: " + e.getMessage();
        }
    }

    public GeneratedSection getGeneratedSection(String id) {
        try {
            GetRequest request = new GetRequest.Builder()
                    .index(ElasticsearchInitializationService.GENERATED_SECTIONS_INDEX)
                    .id(id)
                    .build();
            GetResponse<GeneratedSection> response = elasticsearchClient.get(request, GeneratedSection.class);
            return response.found() ? response.source() : null;
        } catch (Exception e) {
            System.err.println("Error retrieving section: " + e.getMessage());
            return null;
        }
    }

    public String updateGeneratedSection(String id, GeneratedSection section) {
        try {
            section.setId(id);
            UpdateRequest<GeneratedSection, GeneratedSection> request = new UpdateRequest.Builder<GeneratedSection, GeneratedSection>()
                    .index(ElasticsearchInitializationService.GENERATED_SECTIONS_INDEX)
                    .id(id)
                    .doc(section)
                    .build();
            elasticsearchClient.update(request, GeneratedSection.class);
            return "Section " + id + " updated successfully";
        } catch (Exception e) {
            return "Error updating section: " + e.getMessage();
        }
    }

    public String deleteGeneratedSection(String id) {
        try {
            DeleteRequest request = new DeleteRequest.Builder()
                    .index(ElasticsearchInitializationService.GENERATED_SECTIONS_INDEX)
                    .id(id)
                    .build();
            elasticsearchClient.delete(request);
            return "Section deleted successfully";
        } catch (Exception e) {
            return "Error deleting section: " + e.getMessage();
        }
    }

    // ============= CRUD for ManagerPrompts =============

    public String createManagerPrompt(ManagerPrompt prompt) {
        try {
            if (prompt.getId() == null) {
                prompt.setId(UUID.randomUUID().toString());
            }
            IndexRequest<ManagerPrompt> request = new IndexRequest.Builder<ManagerPrompt>()
                    .index(ElasticsearchInitializationService.MANAGER_PROMPTS_INDEX)
                    .id(prompt.getId())
                    .document(prompt)
                    .build();
            elasticsearchClient.index(request);
            return "Manager prompt created with ID: " + prompt.getId();
        } catch (Exception e) {
            return "Error creating prompt: " + e.getMessage();
        }
    }

    public ManagerPrompt getManagerPrompt(String id) {
        try {
            GetRequest request = new GetRequest.Builder()
                    .index(ElasticsearchInitializationService.MANAGER_PROMPTS_INDEX)
                    .id(id)
                    .build();
            GetResponse<ManagerPrompt> response = elasticsearchClient.get(request, ManagerPrompt.class);
            return response.found() ? response.source() : null;
        } catch (Exception e) {
            System.err.println("Error retrieving prompt: " + e.getMessage());
            return null;
        }
    }

    public String updateManagerPrompt(String id, ManagerPrompt prompt) {
        try {
            prompt.setId(id);
            UpdateRequest<ManagerPrompt, ManagerPrompt> request = new UpdateRequest.Builder<ManagerPrompt, ManagerPrompt>()
                    .index(ElasticsearchInitializationService.MANAGER_PROMPTS_INDEX)
                    .id(id)
                    .doc(prompt)
                    .build();
            elasticsearchClient.update(request, ManagerPrompt.class);
            return "Prompt " + id + " updated successfully";
        } catch (Exception e) {
            return "Error updating prompt: " + e.getMessage();
        }
    }

    public String deleteManagerPrompt(String id) {
        try {
            DeleteRequest request = new DeleteRequest.Builder()
                    .index(ElasticsearchInitializationService.MANAGER_PROMPTS_INDEX)
                    .id(id)
                    .build();
            elasticsearchClient.delete(request);
            return "Prompt deleted successfully";
        } catch (Exception e) {
            return "Error deleting prompt: " + e.getMessage();
        }
    }

    // ============= COMPLEX QUERIES =============

    /**
     * COMPLEX QUERY 1: Full-Text Search + Filter + Sort
     * Find generated sections with specific text, filter by status, and sort by confidence
     */
    public List<GeneratedSection> complexQuery1_FullTextWithFilterAndSort(String searchText, String status) {
        try {
            List<GeneratedSection> results = new ArrayList<>();
            SearchRequest request = new SearchRequest.Builder()
                    .index(ElasticsearchInitializationService.GENERATED_SECTIONS_INDEX)
                    .query(q -> q.bool(b -> b
                            .must(m -> m.multiMatch(mm -> mm
                                    .query(searchText)
                                    .fields("generatedText", "sectionName")
                            ))
                            .filter(f -> f.term(t -> t.field("status").value(status)))
                    ))
                    .sort(s -> s.field(f -> f.field("confidenceScore").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                    .size(100)
                    .build();

            SearchResponse<GeneratedSection> response = elasticsearchClient.search(request, GeneratedSection.class);
            for (Hit<GeneratedSection> hit : response.hits().hits()) {
                results.add(hit.source());
            }
            return results;
        } catch (Exception e) {
            System.err.println("Error in complex query 1: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * COMPLEX QUERY 2: Multiple Conditions + Term Aggregation
     * Group manager prompts by category and count them, with effectiveness filter
     */
    public Map<String, Long> complexQuery2_MultipleConditionsWithTermAggregation(float minEffectiveness) {
        try {
            Map<String, Long> results = new HashMap<>();
            SearchRequest request = new SearchRequest.Builder()
                    .index(ElasticsearchInitializationService.MANAGER_PROMPTS_INDEX)
                    .query(q -> q.range(r -> r
                            .field("effectivenessScore")
                            .gte(co.elastic.clients.json.JsonData.of(minEffectiveness))
                    ))
                    .aggregations("categories", a -> a.terms(t -> t.field("category")))
                    .size(0)
                    .build();

            SearchResponse<ManagerPrompt> response = elasticsearchClient.search(request, ManagerPrompt.class);
            if (response.aggregations() != null && response.aggregations().containsKey("categories")) {
                var termsBucket = response.aggregations().get("categories").sterms();
                for (var bucket : termsBucket.buckets().array()) {
                    results.put(bucket.key().stringValue(), bucket.docCount());
                }
            }
            return results;
        } catch (Exception e) {
            System.err.println("Error in complex query 2: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * COMPLEX QUERY 3: Text Search + Date Range + Sub-Aggregation
     * Find sections by text within date range, aggregate by field with average score
     */
    public Map<String, Double> complexQuery3_TextSearchWithDateRangeAndSubAggregation(
            String searchText, long startDate, long endDate) {
        try {
            Map<String, Double> results = new HashMap<>();
            SearchRequest request = new SearchRequest.Builder()
                    .index(ElasticsearchInitializationService.GENERATED_SECTIONS_INDEX)
                    .query(q -> q.bool(b -> b
                            .must(m -> m.match(ma -> ma.field("generatedText").query(searchText)))
                            .filter(f -> f.range(r -> r
                                    .field("createdAt")
                                    .gte(co.elastic.clients.json.JsonData.of(startDate))
                                    .lte(co.elastic.clients.json.JsonData.of(endDate))
                            ))
                    ))
                    .aggregations("byField", a -> a.terms(t -> t.field("researchField"))
                            .aggregations("avgConfidence", sa -> sa.avg(av -> av.field("confidenceScore")))
                    )
                    .size(0)
                    .build();

            SearchResponse<GeneratedSection> response = elasticsearchClient.search(request, GeneratedSection.class);
            if (response.aggregations() != null && response.aggregations().containsKey("byField")) {
                var termsBucket = response.aggregations().get("byField").sterms();
                for (var bucket : termsBucket.buckets().array()) {
                    if (bucket.aggregations() != null && bucket.aggregations().containsKey("avgConfidence")) {
                        var avg = bucket.aggregations().get("avgConfidence").avg();
                        results.put(bucket.key().stringValue(), avg.value());
                    }
                }
            }
            return results;
        } catch (Exception e) {
            System.err.println("Error in complex query 3: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
