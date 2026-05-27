package org.example.cassandraservice.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.example.cassandraservice.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class CassandraService {

    @Autowired
    private CqlSession cqlSession;

    private final String KEYSPACE = CassandraInitializationService.KEYSPACE;

    // ============= CRUD: LLM Requests =============

    public String createLlmRequest(LlmRequestByResearcher request) {
        String query = "INSERT INTO " + KEYSPACE + "." + CassandraInitializationService.TABLE_LLM_REQUESTS +
                " (researcher_id, request_timestamp, request_id, document_id, request_type, token_count, response_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(
                request.getResearcherId(), request.getRequestTimestamp(), request.getRequestId(),
                request.getDocumentId(), request.getRequestType(), request.getTokenCount(),
                request.getResponseTime(), request.getStatus()
        );
        cqlSession.execute(bound);
        return "LLM Request created";
    }

    public List<LlmRequestByResearcher> getLlmRequestsByResearcher(String researcherId) {
        String query = "SELECT * FROM " + KEYSPACE + "." + CassandraInitializationService.TABLE_LLM_REQUESTS +
                " WHERE researcher_id = ? LIMIT 100";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(researcherId);
        ResultSet resultSet = cqlSession.execute(bound);

        List<LlmRequestByResearcher> results = new ArrayList<>();
        for (Row row : resultSet) {
            results.add(LlmRequestByResearcher.builder()
                    .researcherId(row.getString("researcher_id"))
                    .requestTimestamp(row.getInstant("request_timestamp"))
                    .requestId(row.getUuid("request_id"))
                    .documentId(row.getString("document_id"))
                    .requestType(row.getString("request_type"))
                    .tokenCount(row.getInt("token_count"))
                    .responseTime(row.getFloat("response_time"))
                    .status(row.getString("status"))
                    .build());
        }
        return results;
    }

    public String updateLlmRequestStatus(String researcherId, Instant timestamp, UUID requestId, String newStatus) {
        String query = "UPDATE " + KEYSPACE + "." + CassandraInitializationService.TABLE_LLM_REQUESTS +
                " SET status = ? WHERE researcher_id = ? AND request_timestamp = ? AND request_id = ?";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(newStatus, researcherId, timestamp, requestId);
        cqlSession.execute(bound);
        return "LLM Request status updated";
    }

    public String deleteLlmRequest(String researcherId, Instant timestamp, UUID requestId) {
        String query = "DELETE FROM " + KEYSPACE + "." + CassandraInitializationService.TABLE_LLM_REQUESTS +
                " WHERE researcher_id = ? AND request_timestamp = ? AND request_id = ?";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(researcherId, timestamp, requestId);
        cqlSession.execute(bound);
        return "LLM Request deleted";
    }

    // ============= CRUD: Feedbacks =============

    public String createFeedback(FeedbackByManager feedback) {
        String query = "INSERT INTO " + KEYSPACE + "." + CassandraInitializationService.TABLE_FEEDBACK +
                " (manager_id, feedback_date, feedback_id, research_field, rating, comments, action_required) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(
                feedback.getManagerId(), feedback.getFeedbackDate(), feedback.getFeedbackId(),
                feedback.getResearchField(), feedback.getRating(), feedback.getComments(),
                feedback.getActionRequired()
        );
        cqlSession.execute(bound);
        return "Feedback created";
    }

    public List<FeedbackByManager> getFeedbacksByManager(String managerId) {
        String query = "SELECT * FROM " + KEYSPACE + "." + CassandraInitializationService.TABLE_FEEDBACK +
                " WHERE manager_id = ? LIMIT 100";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(managerId);
        ResultSet resultSet = cqlSession.execute(bound);

        List<FeedbackByManager> results = new ArrayList<>();
        for (Row row : resultSet) {
            results.add(FeedbackByManager.builder()
                    .managerId(row.getString("manager_id"))
                    .feedbackDate(row.getInstant("feedback_date"))
                    .feedbackId(row.getUuid("feedback_id"))
                    .researchField(row.getString("research_field"))
                    .rating(row.getInt("rating"))
                    .comments(row.getString("comments"))
                    .actionRequired(row.getString("action_required"))
                    .build());
        }
        return results;
    }

    public String updateFeedbackRating(String managerId, Instant feedbackDate, UUID feedbackId, int newRating) {
        String query = "UPDATE " + KEYSPACE + "." + CassandraInitializationService.TABLE_FEEDBACK +
                " SET rating = ? WHERE manager_id = ? AND feedback_date = ? AND feedback_id = ?";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(newRating, managerId, feedbackDate, feedbackId);
        cqlSession.execute(bound);
        return "Feedback rating updated";
    }

    public String deleteFeedback(String managerId, Instant feedbackDate, UUID feedbackId) {
        String query = "DELETE FROM " + KEYSPACE + "." + CassandraInitializationService.TABLE_FEEDBACK +
                " WHERE manager_id = ? AND feedback_date = ? AND feedback_id = ?";
        
        PreparedStatement prepared = cqlSession.prepare(query);
        BoundStatement bound = prepared.bind(managerId, feedbackDate, feedbackId);
        cqlSession.execute(bound);
        return "Feedback deleted";
    }

    // ============= COMPLEX QUERIES with GROUP BY and AGGREGATION =============

    /**
     * QUERY 1: Count LLM requests by researcher with status filter
     * Shows which researchers made the most requests
     */
    public Map<String, Long> complexQuery1_CountRequestsByResearcherWithStatusFilter(String status) {
        String query = "SELECT researcher_id, COUNT(*) as request_count FROM " + KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_LLM_REQUESTS +
                       " WHERE status = ? ALLOW FILTERING";
        
        Map<String, Long> results = new HashMap<>();
        try {
            PreparedStatement prepared = cqlSession.prepare(query);
            BoundStatement bound = prepared.bind(status);
            ResultSet resultSet = cqlSession.execute(bound);
            
            for (Row row : resultSet) {
                results.put(row.getString("researcher_id"), row.getLong("request_count"));
            }
        } catch (Exception e) {
            System.err.println("Query 1 error: " + e.getMessage());
        }
        return results;
    }

    /**
     * QUERY 2: Average rating by research field from feedbacks
     * Shows effectiveness of feedback per research field
     */
    public Map<String, Double> complexQuery2_AverageRatingByResearchField() {
        String query = "SELECT research_field, AVG(rating) as avg_rating FROM " + KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_FEEDBACK +
                       " GROUP BY research_field ALLOW FILTERING";
        
        Map<String, Double> results = new HashMap<>();
        try {
            ResultSet resultSet = cqlSession.execute(query);
            
            for (Row row : resultSet) {
                results.put(row.getString("research_field"), row.getDouble("avg_rating"));
            }
        } catch (Exception e) {
            System.err.println("Query 2 error: " + e.getMessage());
        }
        return results;
    }

    /**
     * QUERY 3: Count regenerations per section
     * Shows which sections are most frequently regenerated
     */
    public Map<String, Long> complexQuery3_CountRegenerationsBySection() {
        String query = "SELECT section_id, COUNT(*) as regen_count FROM " + KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_REGENERATIONS +
                       " GROUP BY section_id ALLOW FILTERING";
        
        Map<String, Long> results = new HashMap<>();
        try {
            ResultSet resultSet = cqlSession.execute(query);
            
            for (Row row : resultSet) {
                results.put(row.getString("section_id"), row.getLong("regen_count"));
            }
        } catch (Exception e) {
            System.err.println("Query 3 error: " + e.getMessage());
        }
        return results;
    }

    /**
     * QUERY 4: Filter and display document status records by date
     * Shows all documents created on a specific date
     */
    public List<DocumentStatusByDate> complexQuery4_GetDocumentsByDateWithStatus(LocalDate date, String status) {
        String query = "SELECT * FROM " + KEYSPACE + "." + CassandraInitializationService.TABLE_DOCUMENT_STATUS +
                " WHERE document_date = ? AND current_status = ? ALLOW FILTERING";
        
        List<DocumentStatusByDate> results = new ArrayList<>();
        try {
            PreparedStatement prepared = cqlSession.prepare(query);
            BoundStatement bound = prepared.bind(date, status);
            ResultSet resultSet = cqlSession.execute(bound);
            
            for (Row row : resultSet) {
                results.add(DocumentStatusByDate.builder()
                        .documentDate(row.getLocalDate("document_date"))
                        .documentId(row.getString("document_id"))
                        .statusId(row.getUuid("status_id"))
                        .currentStatus(row.getString("current_status"))
                        .ownerResearcherId(row.getString("owner_researcher_id"))
                        .sectionCount(row.getInt("section_count"))
                        .lastModifiedBy(row.getString("last_modified_by"))
                        .build());
            }
        } catch (Exception e) {
            System.err.println("Query 4 error: " + e.getMessage());
        }
        return results;
    }

    /**
     * QUERY 5: Average effectiveness score of prompts by template
     * Shows which prompt templates are most effective
     */
    public Map<String, Double> complexQuery5_AverageEffectivenessByPromptTemplate() {
        String query = "SELECT prompt_template_id, AVG(average_effectiveness) as avg_effectiveness FROM " + KEYSPACE + "." + 
                       CassandraInitializationService.TABLE_PROMPT_USAGE +
                       " GROUP BY prompt_template_id ALLOW FILTERING";
        
        Map<String, Double> results = new HashMap<>();
        try {
            ResultSet resultSet = cqlSession.execute(query);
            
            for (Row row : resultSet) {
                results.put(row.getString("prompt_template_id"), row.getDouble("avg_effectiveness"));
            }
        } catch (Exception e) {
            System.err.println("Query 5 error: " + e.getMessage());
        }
        return results;
    }
}
