package org.example.elasticsearchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchInitializationService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private DataSeederService dataSeederService;

    public static final String GENERATED_SECTIONS_INDEX = "generated_sections";
    public static final String MANAGER_PROMPTS_INDEX = "manager_prompts";

    @PostConstruct
    public void init() {
        try {
            createIndices();
            if (dataSeederService.getIndexCount(GENERATED_SECTIONS_INDEX) < 1000) {
                dataSeederService.seedData();
            } else {
                System.out.println("Elasticsearch indices already populated (1000+ records), skipping seeding.");
            }
        } catch (Exception e) {
            System.err.println("Elasticsearch initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createIndices() throws Exception {
        createIndexIfNotExists(GENERATED_SECTIONS_INDEX);
        createIndexIfNotExists(MANAGER_PROMPTS_INDEX);
    }

    private void createIndexIfNotExists(String indexName) throws Exception {
        ExistsRequest existsRequest = new ExistsRequest.Builder().index(indexName).build();
        boolean indexExists = elasticsearchClient.indices().exists(existsRequest).value();

        if (!indexExists) {
            String mappingJson = getMapping(indexName);
            CreateIndexRequest createRequest = new CreateIndexRequest.Builder()
                    .index(indexName)
                    .withJson(new java.io.StringReader(mappingJson))
                    .build();
            elasticsearchClient.indices().create(createRequest);
            System.out.println("Created index: " + indexName);
        }
    }

    private String getMapping(String indexName) {
        if (GENERATED_SECTIONS_INDEX.equals(indexName)) {
            return """
                    {
                      "settings": {
                        "number_of_shards": 1,
                        "number_of_replicas": 0
                      },
                      "mappings": {
                        "properties": {
                          "id": { "type": "keyword" },
                          "sectionName": { "type": "text", "analyzer": "standard" },
                          "generatedText": { "type": "text", "analyzer": "standard" },
                          "researchField": { "type": "keyword" },
                          "confidenceScore": { "type": "float" },
                          "generationCount": { "type": "integer" },
                          "createdAt": { "type": "long" },
                          "status": { "type": "keyword" }
                        }
                      }
                    }
                    """;
        } else {
            return """
                    {
                      "settings": {
                        "number_of_shards": 1,
                        "number_of_replicas": 0
                      },
                      "mappings": {
                        "properties": {
                          "id": { "type": "keyword" },
                          "promptName": { "type": "text", "analyzer": "standard" },
                          "promptTemplate": { "type": "text", "analyzer": "standard" },
                          "category": { "type": "keyword" },
                          "usageCount": { "type": "integer" },
                          "effectivenessScore": { "type": "float" },
                          "department": { "type": "keyword" },
                          "createdAt": { "type": "long" },
                          "tags": { "type": "keyword" }
                        }
                      }
                    }
                    """;
        }
    }
}
