package org.example.elasticsearchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.elasticsearchservice.model.GeneratedSection;
import org.example.elasticsearchservice.model.ManagerPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class DataSeederService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    //private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private final String[] researchFields = {"Astrophysics", "Bio-engineering", "Quantum Mechanics", "Neuroscience", "Climatology", "Cybersecurity", "Linguistics", "Pharmacology", "Robotics", "Renewable Energy"};
    private final String[] statuses = {"DRAFT", "APPROVED", "REJECTED"};
    private final String[] categories = {"General", "Technical", "Strategic", "Operational", "Creative"};
    private final String[] departments = {"Research", "Engineering", "Management", "Innovation", "Quality Assurance"};

    public void seedData() {
        System.out.println("Starting Elasticsearch seeding (1000+ records per index)...");
        try {
            seedGeneratedSections(1000);
            seedManagerPrompts(1000);
            System.out.println("Elasticsearch seeding completed successfully!");
        } catch (Exception e) {
            System.err.println("Seeding error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedGeneratedSections(int total) throws Exception {
        List<BulkOperation> operations = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            GeneratedSection section = GeneratedSection.builder()
                    .id(UUID.randomUUID().toString())
                    .sectionName("Section_" + researchFields[random.nextInt(researchFields.length)] + "_" + i)
                    .generatedText(generateRandomText("section"))
                    .researchField(researchFields[random.nextInt(researchFields.length)])
                    .confidenceScore(0.5f + random.nextFloat() * 0.5f)
                    .generationCount(random.nextInt(20) + 1)
                    .createdAt(System.currentTimeMillis() - random.nextLong(86400000L * 30))
                    .status(statuses[random.nextInt(statuses.length)])
                    .build();

            operations.add(new BulkOperation.Builder()
                    .index(idx -> idx
                            .index(ElasticsearchInitializationService.GENERATED_SECTIONS_INDEX)
                            .id(section.getId())
                            .document(section)
                    )
                    .build());

            if (operations.size() >= 500) {
                executeBulkInsert(operations);
                operations.clear();
                System.out.println("[GeneratedSections] Batch inserted: " + (i + 1));
            }
        }

        if (!operations.isEmpty()) {
            executeBulkInsert(operations);
            System.out.println("[GeneratedSections] Final batch inserted");
        }
    }

    private void seedManagerPrompts(int total) throws Exception {
        List<BulkOperation> operations = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            ManagerPrompt prompt = ManagerPrompt.builder()
                    .id(UUID.randomUUID().toString())
                    .promptName("Template_" + categories[random.nextInt(categories.length)] + "_" + i)
                    .promptTemplate(generateRandomText("prompt"))
                    .category(categories[random.nextInt(categories.length)])
                    .usageCount(random.nextInt(500))
                    .effectivenessScore(0.6f + random.nextFloat() * 0.4f)
                    .department(departments[random.nextInt(departments.length)])
                    .createdAt(System.currentTimeMillis() - random.nextLong(86400000L * 60))
                    .tags(generateTags())
                    .build();

            operations.add(new BulkOperation.Builder()
                    .index(idx -> idx
                            .index(ElasticsearchInitializationService.MANAGER_PROMPTS_INDEX)
                            .id(prompt.getId())
                            .document(prompt)
                    )
                    .build());

            if (operations.size() >= 500) {
                executeBulkInsert(operations);
                operations.clear();
                System.out.println("[ManagerPrompts] Batch inserted: " + (i + 1));
            }
        }

        if (!operations.isEmpty()) {
            executeBulkInsert(operations);
            System.out.println("[ManagerPrompts] Final batch inserted");
        }
    }

    private void executeBulkInsert(List<BulkOperation> operations) throws Exception {
        BulkRequest request = new BulkRequest.Builder()
                .operations(operations)
                .build();
        elasticsearchClient.bulk(request);
    }

    private String generateRandomText(String type) {
        String[] textSamples = {
                "Advanced analysis reveals significant patterns in the data",
                "The research demonstrates novel approaches to problem-solving",
                "Critical evaluation shows promising results with limitations",
                "Comprehensive study indicates potential applications in the field",
                "Empirical evidence supports the hypothesis with statistical significance"
        };
        return textSamples[random.nextInt(textSamples.length)];
    }

    private String generateTags() {
        String[] tags = {"research", "analysis", "innovation", "quality", "efficiency", "documentation"};
        StringBuilder sb = new StringBuilder();
        int tagCount = random.nextInt(3) + 1;
        for (int i = 0; i < tagCount; i++) {
            sb.append(tags[random.nextInt(tags.length)]).append(",");
        }
        return sb.toString().replaceAll(",$", "");
    }

    public long getIndexCount(String indexName) {
        try {
            var response = elasticsearchClient.count(c -> c.index(indexName));
            return response.count();
        } catch (Exception e) {
            return 0;
        }
    }
}
