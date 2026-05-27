package org.example.cassandraservice.service;

import com.datastax.oss.driver.api.core.CqlSession;
//import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CassandraInitializationService {

    @Autowired
    private CqlSession cqlSession;

    @Autowired
    private DataSeederService dataSeederService;

    public static final String KEYSPACE = "prompt_analytics";
    public static final String TABLE_LLM_REQUESTS = "llm_requests_by_researcher";
    public static final String TABLE_FEEDBACK = "feedbacks_by_manager";
    public static final String TABLE_REGENERATIONS = "regenerations_by_section";
    public static final String TABLE_DOCUMENT_STATUS = "document_status_by_date";
    public static final String TABLE_PROMPT_USAGE = "prompt_usage_by_template";

    @PostConstruct
    public void init() {
        try {
            createKeyspace();
            createTables();
            
            // Check if data needs seeding
            if (getTableRowCount(TABLE_LLM_REQUESTS) < 200) {
                dataSeederService.seedData();
            } else {
                System.out.println("Cassandra tables already populated (200+ records), skipping seeding.");
            }
        } catch (Exception e) {
            System.err.println("Cassandra initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createKeyspace() {
        try {
            String createKeyspaceQuery = "CREATE KEYSPACE IF NOT EXISTS " + KEYSPACE + " WITH replication = " +
                    "{'class':'SimpleStrategy', 'replication_factor':1}";
            cqlSession.execute(createKeyspaceQuery);
            System.out.println("Keyspace " + KEYSPACE + " ready");
        } catch (Exception e) {
            System.err.println("Error creating keyspace: " + e.getMessage());
        }
    }

    private void createTables() {
        createLlmRequestsTable();
        createFeedbacksTable();
        createRegenerationsTable();
        createDocumentStatusTable();
        createPromptUsageTable();
    }

    private void createLlmRequestsTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + KEYSPACE + "." + TABLE_LLM_REQUESTS + " (" +
                "researcher_id TEXT, " +
                "request_timestamp TIMESTAMP, " +
                "request_id UUID, " +
                "document_id TEXT, " +
                "request_type TEXT, " +
                "token_count INT, " +
                "response_time FLOAT, " +
                "status TEXT, " +
                "PRIMARY KEY (researcher_id, request_timestamp, request_id)" +
                ") WITH CLUSTERING ORDER BY (request_timestamp DESC)";
        cqlSession.execute(query);
        System.out.println("Table " + TABLE_LLM_REQUESTS + " created");
    }

    private void createFeedbacksTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + KEYSPACE + "." + TABLE_FEEDBACK + " (" +
                "manager_id TEXT, " +
                "feedback_date TIMESTAMP, " +
                "feedback_id UUID, " +
                "research_field TEXT, " +
                "rating INT, " +
                "comments TEXT, " +
                "action_required TEXT, " +
                "PRIMARY KEY (manager_id, feedback_date, feedback_id)" +
                ") WITH CLUSTERING ORDER BY (feedback_date DESC)";
        cqlSession.execute(query);
        System.out.println("Table " + TABLE_FEEDBACK + " created");
    }

    private void createRegenerationsTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + KEYSPACE + "." + TABLE_REGENERATIONS + " (" +
                "section_id TEXT, " +
                "regeneration_time TIMESTAMP, " +
                "regeneration_id UUID, " +
                "researcher_id TEXT, " +
                "previous_text TEXT, " +
                "new_text TEXT, " +
                "confidence INT, " +
                "reason TEXT, " +
                "PRIMARY KEY (section_id, regeneration_time, regeneration_id)" +
                ") WITH CLUSTERING ORDER BY (regeneration_time DESC)";
        cqlSession.execute(query);
        System.out.println("Table " + TABLE_REGENERATIONS + " created");
    }

    private void createDocumentStatusTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + KEYSPACE + "." + TABLE_DOCUMENT_STATUS + " (" +
                "document_date DATE, " +
                "document_id TEXT, " +
                "status_id UUID, " +
                "current_status TEXT, " +
                "owner_researcher_id TEXT, " +
                "section_count INT, " +
                "last_modified_by TEXT, " +
                "PRIMARY KEY (document_date, document_id, status_id)" +
                ") WITH CLUSTERING ORDER BY (document_id ASC, status_id DESC)";
        cqlSession.execute(query);
        System.out.println("Table " + TABLE_DOCUMENT_STATUS + " created");
    }

    private void createPromptUsageTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + KEYSPACE + "." + TABLE_PROMPT_USAGE + " (" +
                "prompt_template_id TEXT, " +
                "researcher_id TEXT, " +
                "usage_id UUID, " +
                "usage_count INT, " +
                "average_effectiveness FLOAT, " +
                "last_used_date TEXT, " +
                "is_active BOOLEAN, " +
                "PRIMARY KEY (prompt_template_id, researcher_id, usage_id)" +
                ") WITH CLUSTERING ORDER BY (researcher_id ASC)";
        cqlSession.execute(query);
        System.out.println("Table " + TABLE_PROMPT_USAGE + " created");
    }

    private long getTableRowCount(String tableName) {
        try {
            var result = cqlSession.execute("SELECT COUNT(*) as count FROM " + KEYSPACE + "." + tableName);
            var row = result.one();
            if (row != null) {
                return row.getLong("count");
            }
        } catch (Exception e) {
            System.out.println("Could not fetch row count: " + e.getMessage());
        }
        return 0;
    }
}
