package org.example.cassandraservice.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class CassandraService {

    @Autowired
    private CqlSession cqlSession;

    private final String KEY = "prompt_analytics";

    //CRUD - llm_requests_by_researcher
    @CacheEvict(value = "requestsCountByResearcher", allEntries = true)
    public void insertLlmRequest(String resId, Instant ts, UUID id, String status) {
        String q = "INSERT INTO " + KEY + ".llm_requests_by_researcher " +
                   "(researcher_id, request_timestamp, request_id, status) VALUES (?,?,?,?)";
        cqlSession.execute(cqlSession.prepare(q).bind(resId, ts, id, status));
    }

    public List<Map<String, Object>> getLlmRequestsByResearcher(String resId) {
        String q = "SELECT request_timestamp, request_id, status FROM " + KEY +
                   ".llm_requests_by_researcher WHERE researcher_id = ?";
        ResultSet rs = cqlSession.execute(cqlSession.prepare(q).bind(resId));
        List<Map<String, Object>> results = new ArrayList<>();
        rs.forEach(row -> {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("researcher_id", resId);
            record.put("request_timestamp", row.getInstant("request_timestamp"));
            record.put("request_id", row.getUuid("request_id"));
            record.put("status", row.getString("status"));
            results.add(record);
        });
        return results;
    }

    public List<Map<String, Object>> updateLlmRequestStatus(String resId, UUID requestId,
                                                             Instant ts, String newStatus) {
        String q = "UPDATE " + KEY + ".llm_requests_by_researcher SET status = ? " +
                   "WHERE researcher_id = ? AND request_timestamp = ? AND request_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(newStatus, resId, ts, requestId));
        return getLlmRequestsByResearcherDirect(resId);
    }

    public void deleteLlmRequest(String resId, Instant ts, UUID requestId) {
        String q = "DELETE FROM " + KEY + ".llm_requests_by_researcher " +
                   "WHERE researcher_id = ? AND request_timestamp = ? AND request_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(resId, ts, requestId));
    }

    private List<Map<String, Object>> getLlmRequestsByResearcherDirect(String resId) {
        String q = "SELECT request_timestamp, request_id, status FROM " + KEY +
                   ".llm_requests_by_researcher WHERE researcher_id = ?";
        ResultSet rs = cqlSession.execute(cqlSession.prepare(q).bind(resId));
        List<Map<String, Object>> results = new ArrayList<>();
        rs.forEach(row -> {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("researcher_id", resId);
            record.put("request_timestamp", row.getInstant("request_timestamp"));
            record.put("request_id", row.getUuid("request_id"));
            record.put("status", row.getString("status"));
            results.add(record);
        });
        return results;
    }

    //CRUD - feedbacks_by_field

    public void insertFeedback(String field, UUID id, int rating) {
        String q = "INSERT INTO " + KEY + ".feedbacks_by_field " +
                   "(research_field, feedback_id, rating) VALUES (?,?,?)";
        cqlSession.execute(cqlSession.prepare(q).bind(field, id, rating));
    }

    public List<Map<String, Object>> getFeedbacksByField(String field) {
        String q = "SELECT feedback_id, rating FROM " + KEY +
                   ".feedbacks_by_field WHERE research_field = ?";
        ResultSet rs = cqlSession.execute(cqlSession.prepare(q).bind(field));
        List<Map<String, Object>> results = new ArrayList<>();
        rs.forEach(row -> {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("research_field", field);
            record.put("feedback_id", row.getUuid("feedback_id"));
            record.put("rating", row.getInt("rating"));
            results.add(record);
        });
        return results;
    }

    public void updateFeedbackRating(String field, UUID feedbackId, int newRating) {
        String q = "UPDATE " + KEY + ".feedbacks_by_field SET rating = ? " +
                   "WHERE research_field = ? AND feedback_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(newRating, field, feedbackId));
    }

    public void deleteFeedback(String field, UUID feedbackId) {
        String q = "DELETE FROM " + KEY + ".feedbacks_by_field " +
                   "WHERE research_field = ? AND feedback_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(field, feedbackId));
    }

    //CRUD - regenerations_by_section

    public void insertRegeneration(String secId, UUID id, String resId) {
        String q = "INSERT INTO " + KEY + ".regenerations_by_section " +
                   "(section_id, regeneration_id, researcher_id) VALUES (?,?,?)";
        cqlSession.execute(cqlSession.prepare(q).bind(secId, id, resId));
    }

    public List<Map<String, Object>> getRegenerationsBySection(String secId) {
        String q = "SELECT regeneration_id, researcher_id FROM " + KEY +
                   ".regenerations_by_section WHERE section_id = ?";
        ResultSet rs = cqlSession.execute(cqlSession.prepare(q).bind(secId));
        List<Map<String, Object>> results = new ArrayList<>();
        rs.forEach(row -> {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("section_id", secId);
            record.put("regeneration_id", row.getUuid("regeneration_id"));
            record.put("researcher_id", row.getString("researcher_id"));
            results.add(record);
        });
        return results;
    }

    public void updateRegenerationResearcher(String secId, UUID regenId, String newResId) {
        String q = "UPDATE " + KEY + ".regenerations_by_section SET researcher_id = ? " +
                   "WHERE section_id = ? AND regeneration_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(newResId, secId, regenId));
    }

    public void deleteRegeneration(String secId, UUID regenId) {
        String q = "DELETE FROM " + KEY + ".regenerations_by_section " +
                   "WHERE section_id = ? AND regeneration_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(secId, regenId));
    }

    //CRUD - document_status_by_date

    public void insertDocStatus(LocalDate date, String status, String docId) {
        String q = "INSERT INTO " + KEY + ".document_status_by_date " +
                   "(document_date, current_status, document_id) VALUES (?,?,?)";
        cqlSession.execute(cqlSession.prepare(q).bind(date, status, docId));
    }

    public Map<String, Object> getDocStatus(LocalDate date, String status, String docId) {
        String q = "SELECT document_id, current_status, document_date FROM " + KEY +
                   ".document_status_by_date WHERE document_date = ? AND current_status = ? AND document_id = ?";
        Row row = cqlSession.execute(cqlSession.prepare(q).bind(date, status, docId)).one();
        if (row == null) return Collections.emptyMap();
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("document_date", row.getLocalDate("document_date"));
        record.put("current_status", row.getString("current_status"));
        record.put("document_id", row.getString("document_id"));
        return record;
    }

    public void updateDocStatus(LocalDate date, String oldStatus, String newStatus, String docId) {
        // U Cassandri PRIMARY KEY nije menjiv — brišemo stari slog i upisujemo novi
        String del = "DELETE FROM " + KEY + ".document_status_by_date " +
                     "WHERE document_date = ? AND current_status = ? AND document_id = ?";
        cqlSession.execute(cqlSession.prepare(del).bind(date, oldStatus, docId));

        String ins = "INSERT INTO " + KEY + ".document_status_by_date " +
                     "(document_date, current_status, document_id) VALUES (?,?,?)";
        cqlSession.execute(cqlSession.prepare(ins).bind(date, newStatus, docId));
    }

    public void deleteDocStatus(LocalDate date, String status, String docId) {
        String q = "DELETE FROM " + KEY + ".document_status_by_date " +
                   "WHERE document_date = ? AND current_status = ? AND document_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(date, status, docId));
    }

    //CRUD - prompt_usage_by_template

    public void insertPromptUsage(String tempId, UUID id, float eff) {
        String q = "INSERT INTO " + KEY + ".prompt_usage_by_template " +
                   "(prompt_template_id, usage_id, effectiveness) VALUES (?,?,?)";
        cqlSession.execute(cqlSession.prepare(q).bind(tempId, id, eff));
    }

    public List<Map<String, Object>> getPromptUsageByTemplate(String tempId) {
        String q = "SELECT usage_id, effectiveness FROM " + KEY +
                   ".prompt_usage_by_template WHERE prompt_template_id = ?";
        ResultSet rs = cqlSession.execute(cqlSession.prepare(q).bind(tempId));
        List<Map<String, Object>> results = new ArrayList<>();
        rs.forEach(row -> {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("prompt_template_id", tempId);
            record.put("usage_id", row.getUuid("usage_id"));
            record.put("effectiveness", row.getFloat("effectiveness"));
            results.add(record);
        });
        return results;
    }

    public void updatePromptEffectiveness(String tempId, UUID usageId, float newEff) {
        String q = "UPDATE " + KEY + ".prompt_usage_by_template SET effectiveness = ? " +
                   "WHERE prompt_template_id = ? AND usage_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(newEff, tempId, usageId));
    }

    public void deletePromptUsage(String tempId, UUID usageId) {
        String q = "DELETE FROM " + KEY + ".prompt_usage_by_template " +
                   "WHERE prompt_template_id = ? AND usage_id = ?";
        cqlSession.execute(cqlSession.prepare(q).bind(tempId, usageId));
    }

    //UPIT 1 (Agregacija) — Broj zahteva po istrazivacu.
  
    @Cacheable(value = "requestsCountByResearcher")
    public Map<String, Long> query1_CountRequestsByResearcher() {
        String q = "SELECT researcher_id, COUNT(*) as total FROM " + KEY +
                   ".llm_requests_by_researcher GROUP BY researcher_id";
        ResultSet rs = cqlSession.execute(q);
        Map<String, Long> res = new LinkedHashMap<>();
        rs.forEach(row -> res.put(row.getString("researcher_id"), row.getLong("total")));
        return res;
    }

    //UPIT 2 (Agregacija) — Prosecna ocena po naucnom polju.
     
    @Cacheable(value = "avgRatingByField")
    public Map<String, Double> query2_AvgRatingByField() {
       String q = "SELECT research_field, AVG(rating) as avg_rating FROM " + KEY +
               ".feedbacks_by_field GROUP BY research_field";
    ResultSet rs = cqlSession.execute(q);
    Map<String, Double> res = new LinkedHashMap<>();
    
    rs.forEach(row -> {
        String field = row.getString("research_field");
        Object val = row.getObject("avg_rating");
        double avg = 0.0;
        
        if (val instanceof Number) {
            avg = ((Number) val).doubleValue();
        }
        
        res.put(field, avg);
    });
    return res;
    }

    //UPIT 3 (Agregacija) — Broj regeneracija po sekciji.

    @Cacheable(value = "regenCountBySection")
    public Map<String, Long> query3_CountRegensBySection() {
        String q = "SELECT section_id, COUNT(*) as total FROM " + KEY +
                   ".regenerations_by_section GROUP BY section_id";
        ResultSet rs = cqlSession.execute(q);
        Map<String, Long> res = new LinkedHashMap<>();
        rs.forEach(row -> res.put(row.getString("section_id"), row.getLong("total")));
        return res;
    }

   
     //UPIT 4 (Uslovni prikaz) — Dokumenti za specifican datum i status.
     
    @Cacheable(value = "docsByDateStatus", key = "#date.toString() + '_' + #status")
    public List<String> query4_GetDocsByDateAndStatus(LocalDate date, String status) {
        String q = "SELECT document_id FROM " + KEY +
                   ".document_status_by_date WHERE document_date = ? AND current_status = ?";
        ResultSet rs = cqlSession.execute(cqlSession.prepare(q).bind(date, status));
        List<String> docs = new ArrayList<>();
        rs.forEach(row -> docs.add(row.getString("document_id")));
        return docs;
    }

    //UPIT 5 (Uslovni prikaz) — Sve effectiveness vrednosti za odredjeni prompt template.
     
    @Cacheable(value = "promptUsageByTemplate", key = "#templateId + '_effectiveness'")
    public List<Float> query5_GetEffectivenessByTemplate(String templateId) {
        String q = "SELECT effectiveness FROM " + KEY +
                   ".prompt_usage_by_template WHERE prompt_template_id = ?";
        ResultSet rs = cqlSession.execute(cqlSession.prepare(q).bind(templateId));
        List<Float> results = new ArrayList<>();
        rs.forEach(row -> results.add(row.getFloat("effectiveness")));
        return results;
    }
}