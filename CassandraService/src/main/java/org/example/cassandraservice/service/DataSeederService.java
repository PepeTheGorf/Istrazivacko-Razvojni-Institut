package org.example.cassandraservice.service;

import com.datastax.oss.driver.api.core.CqlSession;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Service
public class DataSeederService {

 @Autowired
    private CqlSession cqlSession;

    @EventListener(ApplicationReadyEvent.class)
    public void seedData() {
        try {
            System.out.println(">>> WAITING 5s FOR TABLES TO SETTLE...");
            Thread.sleep(5000); 

            System.out.println(">>> STARTING DATA SEEDING...");
            Faker faker = new Faker();
            String keyspace = "prompt_analytics";

            for (int i = 0; i < 100; i++) {
                cqlSession.execute("INSERT INTO " + keyspace + ".llm_requests_by_researcher (researcher_id, request_timestamp, request_id, status) VALUES (?,?,?,?)",
                        faker.name().lastName(), Instant.now(), UUID.randomUUID(), "SUCCESS");

                cqlSession.execute("INSERT INTO " + keyspace + ".feedbacks_by_field (research_field, feedback_id, rating) VALUES (?,?,?)",
                        faker.options().option("AI", "Physics", "Biology"), UUID.randomUUID(), faker.number().numberBetween(1, 6));

                cqlSession.execute("INSERT INTO " + keyspace + ".document_status_by_date (document_date, current_status, document_id) VALUES (?,?,?)",
                        LocalDate.now(), faker.options().option("DRAFT", "PUBLISHED"), "doc_" + i);
                
                cqlSession.execute("INSERT INTO " + keyspace + ".regenerations_by_section (section_id, regeneration_id, researcher_id) VALUES (?,?,?)",
                        "sec_" + faker.number().numberBetween(1, 10), UUID.randomUUID(), faker.name().firstName());

                cqlSession.execute("INSERT INTO " + keyspace + ".prompt_usage_by_template (prompt_template_id, usage_id, effectiveness) VALUES (?,?,?)",
                        "temp_" + faker.number().numberBetween(1, 5), UUID.randomUUID(), (float)faker.number().randomDouble(2, 0, 1));
            }
            System.out.println(">>> SEEDING FINISHED SUCCESSFULLY!");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR DURING SEEDING: " + e.getMessage());
            e.printStackTrace(); 
        }
    }
}