package org.example.cassandraservice.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
//import com.datastax.oss.driver.api.core.cql.BoundStatement;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class DataSeederService {

    @Autowired
    private CqlSession cqlSession;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    // Liste za održavanje konzistentnosti (da se ista imena pojavljuju u različitim tabelama)
    private final List<String> researcherNames = new ArrayList<>();
    private final List<String> managerNames = new ArrayList<>();
    private final List<String> promptTemplates = new ArrayList<>();

    private void initializeBaseData() {
        if (researcherNames.isEmpty()) {
            for (int i = 0; i < 200; i++) researcherNames.add(faker.name().fullName());
        }
        if (managerNames.isEmpty()) {
            for (int i = 0; i < 50; i++) managerNames.add(faker.name().fullName());
        }
        if (promptTemplates.isEmpty()) {
            for (int i = 0; i < 30; i++) promptTemplates.add(faker.science().element() + " " + faker.science().tool());
        }
    }

    public void seedData() {
        initializeBaseData();
        System.out.println("Starting realistic seeding...");
        
        // Povećaj brojeve za pravi test performansi (npr. na 10000 ili više)
        seedLlmRequests(5000); 
        seedFeedbacks(2000);
        seedRegenerations(3000);
        seedDocumentStatus(2000);
        seedPromptUsage(1000);
        
        System.out.println("Seeding completed!");
    }

    private void seedLlmRequests(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_LLM_REQUESTS + 
                       " (researcher_id, request_timestamp, request_id, document_id, request_type, token_count, response_time, status) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            String researcher = researcherNames.get(random.nextInt(researcherNames.size()));
            int tokens = 50 + random.nextInt(4000);
            
            // Logička korelacija: Više tokena = sporiji odziv
            float responseTime = (tokens / 800.0f) + random.nextFloat() * 2;
            
            // Bias: 90% šanse za SUCCESS
            String status = (random.nextInt(100) < 90) ? "SUCCESS" : (random.nextBoolean() ? "FAILED" : "TIMEOUT");
            if (status.equals("TIMEOUT")) responseTime = 30.0f;

            cqlSession.execute(prepared.bind(
                researcher, Instant.now().minusSeconds(random.nextLong(1000000)), UUID.randomUUID(),
                "doc_" + random.nextInt(500), random.nextBoolean() ? "GENERATION" : "REGENERATION",
                tokens, responseTime, status
            ));
        }
    }

    private void seedFeedbacks(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_FEEDBACK + 
                       " (manager_id, feedback_date, feedback_id, research_field, rating, comments, action_required) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            int rating = 1 + random.nextInt(5);
            String field = faker.science().element() + " " + faker.job().field(); // Npr. "Oxygen Engineering"
            
            // Logička korelacija: Komentar zavisi od ocene
            String comment;
            if (rating <= 2) comment = "Critical issue: " + faker.lorem().sentence();
            else if (rating == 3) comment = "Average quality. " + faker.lorem().sentence();
            else comment = "Excellent work on " + field + ". " + faker.lorem().sentence();

            cqlSession.execute(prepared.bind(
                managerNames.get(random.nextInt(managerNames.size())), 
                Instant.now().minusSeconds(random.nextLong(1000000)), UUID.randomUUID(),
                field, rating, comment, (rating < 3 ? "YES" : "NO")
            ));
        }
    }

    private void seedRegenerations(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_REGENERATIONS + 
                       " (section_id, regeneration_time, regeneration_id, researcher_id, previous_text, new_text, confidence, reason) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            String previousText = "Draft: " + faker.lorem().paragraph(3); 
            String newText = "Refined: " + faker.lorem().paragraph(6);    

            cqlSession.execute(prepared.bind(
                "sec_" + random.nextInt(1000), 
                Instant.now(), 
                UUID.randomUUID(),
                researcherNames.get(random.nextInt(researcherNames.size())), 
                previousText, 
                newText,
                70 + random.nextInt(30), 
                faker.options().option("Grammar Correction", "Expanding details", "Formal tone adjustment", "Fact checking")
            ));
        }
    }

    private void seedDocumentStatus(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_DOCUMENT_STATUS + 
                       " (document_date, document_id, status_id, current_status, owner_researcher_id, section_count, last_modified_by) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement prepared = cqlSession.prepare(query);

        String[] statuses = {"DRAFT", "IN_REVIEW", "PUBLISHED"};

        for (int i = 0; i < total; i++) {
            String researcher = researcherNames.get(random.nextInt(researcherNames.size()));
            cqlSession.execute(prepared.bind(
                LocalDate.now().minusDays(random.nextInt(60)), "doc_" + random.nextInt(500), UUID.randomUUID(),
                statuses[random.nextInt(3)], researcher, 5 + random.nextInt(25), researcher
            ));
        }
    }

    private void seedPromptUsage(int total) {
        String query = "INSERT INTO " + CassandraInitializationService.KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_PROMPT_USAGE + 
                       " (prompt_template_id, researcher_id, usage_id, usage_count, average_effectiveness, last_used_date, is_active) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement prepared = cqlSession.prepare(query);

        for (int i = 0; i < total; i++) {
            cqlSession.execute(prepared.bind(
                promptTemplates.get(random.nextInt(promptTemplates.size())),
                researcherNames.get(random.nextInt(researcherNames.size())), UUID.randomUUID(),
                1 + random.nextInt(200), random.nextFloat(), 
                LocalDate.now().minusDays(random.nextInt(30)).toString(), random.nextBoolean()
            ));
        }
    }
}