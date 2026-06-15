package org.example.cassandraservice.service;

import com.datastax.oss.driver.api.core.CqlSession;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CassandraInitializationService {

    @Autowired
    private CqlSession cqlSession;

    public static final String KEYSPACE = "prompt_analytics";

    @PostConstruct
    public void init() {
        try {
            Thread.sleep(20000); 
            createKeyspace();
            createTables();
            System.out.println("Cassandra Initialization Finished.");
        } catch (Exception e) {
            System.err.println("Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createKeyspace() {
        cqlSession.execute("CREATE KEYSPACE IF NOT EXISTS " + KEYSPACE + 
            " WITH replication = {'class':'SimpleStrategy', 'replication_factor':1}");
    }

    private void createTables() {
        cqlSession.execute("CREATE TABLE IF NOT EXISTS " + KEYSPACE + ".llm_requests_by_researcher (" +
                "researcher_id TEXT, request_timestamp TIMESTAMP, request_id UUID, status TEXT, tokens INT, " +
                "PRIMARY KEY (researcher_id, request_timestamp, request_id)) " +
                "WITH CLUSTERING ORDER BY (request_timestamp DESC)");

        cqlSession.execute("CREATE TABLE IF NOT EXISTS " + KEYSPACE + ".feedbacks_by_field (" +
                "research_field TEXT, feedback_date TIMESTAMP, feedback_id UUID, rating INT, manager_id TEXT, " +
                "PRIMARY KEY (research_field, feedback_id))");

        cqlSession.execute("CREATE TABLE IF NOT EXISTS " + KEYSPACE + ".regenerations_by_section (" +
                "section_id TEXT, regeneration_time TIMESTAMP, regeneration_id UUID, researcher_id TEXT, " +
                "PRIMARY KEY (section_id, regeneration_id))");

        cqlSession.execute("CREATE TABLE IF NOT EXISTS " + KEYSPACE + ".document_status_by_date (" +
                "document_date DATE, current_status TEXT, document_id TEXT, owner_id TEXT, " +
                "PRIMARY KEY ((document_date, current_status), document_id))");

        cqlSession.execute("CREATE TABLE IF NOT EXISTS " + KEYSPACE + ".prompt_usage_by_template (" +
                "prompt_template_id TEXT, usage_id UUID, effectiveness FLOAT, researcher_id TEXT, " +
                "PRIMARY KEY (prompt_template_id, usage_id))");
    }
}