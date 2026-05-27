package org.example.cassandraservice.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
//import org.example.cassandraservice.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class DataSeederService {

    @Autowired
    private CqlSession cqlSession;

    private final Random random = new Random();
    private final String[] researchFields = {"Astrophysics", "Bio-engineering", "Quantum Mechanics", "Neuroscience", "Climatology", "Cybersecurity"};
    private final String[] statuses = {"SUCCESS", "FAILED", "TIMEOUT"};
    private final String[] documentStatuses = {"DRAFT", "IN_REVIEW", "PUBLISHED"};
    private final String[] actions = {"YES", "NO"};

    public void seedData() {
        System.out.println("Starting Cassandra seeding (1000+ records total)...");
        try {
            seedLlmRequests(200);
            seedFeedbacks(200);
            seedRegenerations(200);
            seedDocumentStatus(200);
            seedPromptUsage(200);
            System.out.println("Cassandra seeding completed successfully!");
        } catch (Exception e) {
            System.err.println("Seeding error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedLlmRequests(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_LLM_REQUESTS + 
                       " (researcher_id, request_timestamp, request_id, document_id, request_type, token_count, response_time, status) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            String researcherId = "researcher_" + (i % 50);
            Instant timestamp = Instant.now().minusSeconds(random.nextLong(2592000L)); // Last 30 days
            UUID requestId = UUID.randomUUID();
            String documentId = "doc_" + (i % 100);
            String requestType = random.nextBoolean() ? "GENERATION" : "REGENERATION";
            int tokenCount = 100 + random.nextInt(1000);
            float responseTime = 0.1f + random.nextFloat() * 5.0f;
            String status = statuses[random.nextInt(statuses.length)];

            BoundStatement bound = prepared.bind(
                    researcherId, timestamp, requestId, documentId, 
                    requestType, tokenCount, responseTime, status
            );
            cqlSession.execute(bound);
        }
        System.out.println("[LlmRequests] Seeded " + total + " records");
    }

    private void seedFeedbacks(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_FEEDBACK + 
                       " (manager_id, feedback_date, feedback_id, research_field, rating, comments, action_required) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            String managerId = "manager_" + (i % 20);
            Instant feedbackDate = Instant.now().minusSeconds(random.nextLong(2592000L));
            UUID feedbackId = UUID.randomUUID();
            String researchField = researchFields[random.nextInt(researchFields.length)];
            int rating = 1 + random.nextInt(5);
            String comments = "Feedback on " + researchField + " - " + (random.nextBoolean() ? "Positive" : "Needs improvement");
            String actionRequired = actions[random.nextInt(actions.length)];

            BoundStatement bound = prepared.bind(
                    managerId, feedbackDate, feedbackId, researchField,
                    rating, comments, actionRequired
            );
            cqlSession.execute(bound);
        }
        System.out.println("[Feedbacks] Seeded " + total + " records");
    }

    private void seedRegenerations(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_REGENERATIONS + 
                       " (section_id, regeneration_time, regeneration_id, researcher_id, previous_text, new_text, confidence, reason) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            String sectionId = "section_" + (i % 150);
            Instant regTime = Instant.now().minusSeconds(random.nextLong(2592000L));
            UUID regenerationId = UUID.randomUUID();
            String researcherId = "researcher_" + (i % 50);
            String previousText = "Original text for section " + sectionId;
            String newText = "Regenerated text for section " + sectionId + " v" + random.nextInt(10);
            int confidence = 50 + random.nextInt(50);
            String reason = random.nextBoolean() ? "User request" : "Quality improvement";

            BoundStatement bound = prepared.bind(
                    sectionId, regTime, regenerationId, researcherId,
                    previousText, newText, confidence, reason
            );
            cqlSession.execute(bound);
        }
        System.out.println("[Regenerations] Seeded " + total + " records");
    }

    private void seedDocumentStatus(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_DOCUMENT_STATUS + 
                       " (document_date, document_id, status_id, current_status, owner_researcher_id, section_count, last_modified_by) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            LocalDate docDate = LocalDate.now().minusDays(random.nextLong(60));
            String documentId = "doc_" + (i % 100);
            UUID statusId = UUID.randomUUID();
            String currentStatus = documentStatuses[random.nextInt(documentStatuses.length)];
            String ownerResearcherId = "researcher_" + (i % 50);
            int sectionCount = 5 + random.nextInt(20);
            String lastModifiedBy = "researcher_" + (i % 50);

            BoundStatement bound = prepared.bind(
                    docDate, documentId, statusId, currentStatus,
                    ownerResearcherId, sectionCount, lastModifiedBy
            );
            cqlSession.execute(bound);
        }
        System.out.println("[DocumentStatus] Seeded " + total + " records");
    }

    private void seedPromptUsage(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_PROMPT_USAGE + 
                       " (prompt_template_id, researcher_id, usage_id, usage_count, average_effectiveness, last_used_date, is_active) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            String promptTemplateId = "prompt_" + (i % 75);
            String researcherId = "researcher_" + (i % 50);
            UUID usageId = UUID.randomUUID();
            int usageCount = 1 + random.nextInt(100);
            float avgEffectiveness = 0.5f + random.nextFloat() * 0.5f;
            String lastUsedDate = LocalDate.now().minusDays(random.nextLong(30)).toString();
            boolean isActive = random.nextBoolean();

            BoundStatement bound = prepared.bind(
                    promptTemplateId, researcherId, usageId,
                    usageCount, avgEffectiveness, lastUsedDate, isActive
            );
            cqlSession.execute(bound);
        }
        System.out.println("[PromptUsage] Seeded " + total + " records");
    }
}
