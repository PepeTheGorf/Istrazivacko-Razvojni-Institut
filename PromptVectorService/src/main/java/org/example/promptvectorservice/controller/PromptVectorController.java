package org.example.promptvectorservice.controller;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.QueryResults;
import io.milvus.param.*;
import io.milvus.param.dml.*;
import io.milvus.param.dml.ranker.RRFRanker;
import io.milvus.orm.iterator.SearchIterator;
import io.milvus.response.QueryResultsWrapper;
import org.example.promptvectorservice.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/vector")
public class PromptVectorController {

    @Autowired
    private MilvusServiceClient milvusClient;

    @Autowired
    private EmbeddingService embeddingService;

    //CRUD OPERACIJE - PROMPT TEMPLATES

    @PostMapping("/template")
    public String createTemplate(@RequestParam String text, @RequestParam String category) {
        long id = System.currentTimeMillis();
        List<Float> pVec = embeddingService.generateEmbedding(text);
        List<Float> sVec = embeddingService.generateEmbedding(category);

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", List.of(id)));
        fields.add(new InsertParam.Field("category", List.of(category)));
        fields.add(new InsertParam.Field("prompt_text", List.of(text)));
        fields.add(new InsertParam.Field("short_summary", List.of(category + " analysis")));
        fields.add(new InsertParam.Field("avg_rating", List.of(5.0f)));
        fields.add(new InsertParam.Field("version", List.of(1)));
        fields.add(new InsertParam.Field("prompt_vector", List.of(pVec)));
        fields.add(new InsertParam.Field("summary_vector", List.of(sVec)));

        milvusClient.insert(InsertParam.newBuilder().withCollectionName("PromptTemplates").withFields(fields).build());
        return "Šablon kreiran sa ID: " + id;
    }

    @DeleteMapping("/template/{id}")
    public String deleteTemplate(@PathVariable Long id) {
        milvusClient.delete(DeleteParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .withExpr("id == " + id).build());
        return "Šablon obrisan!";
    }
 
    //CRUD: RESEARCHER FEEDBACKS
    @PostMapping("/feedback")
    public String createFeedback(@RequestParam String comment, @RequestParam String field, @RequestParam Integer rating) {
        long id = System.currentTimeMillis();
        List<Float> vector = embeddingService.generateEmbedding(comment);

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", List.of(id)));
        fields.add(new InsertParam.Field("rating", List.of(rating)));
        fields.add(new InsertParam.Field("regeneration_count", List.of(0)));
        fields.add(new InsertParam.Field("research_field", List.of(field)));
        fields.add(new InsertParam.Field("feedback_comment", List.of(comment)));
        fields.add(new InsertParam.Field("feedback_vector", List.of(vector)));

        milvusClient.insert(InsertParam.newBuilder().withCollectionName("ResearcherFeedbacks").withFields(fields).build());
        return "Feedback dodat! ID: " + id;
    }

    @DeleteMapping("/feedback/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        milvusClient.delete(DeleteParam.newBuilder().withCollectionName("ResearcherFeedbacks").withExpr("id == " + id).build());
        return "Feedback obrisan!";
    }

    //PROSTI UPIT: Dobavljanje po ID-u
    @GetMapping("/template/{id}")
    public String getTemplate(@PathVariable Long id) {
        R<QueryResults> response = milvusClient.query(QueryParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .withExpr("id == " + id)
                .withOutFields(List.of("prompt_text", "category", "avg_rating"))
                .build());
        return response.getData().toString();
    }

    //PROSTI UPIT: Brojanje loših feedback-ova (Filter + Count)
    @GetMapping("/feedback/count-low-rating")
    public String countLowRatings(@RequestParam String field) {
        R<QueryResults> response = milvusClient.query(QueryParam.newBuilder()
                .withCollectionName("ResearcherFeedbacks")
                .withExpr("research_field == '" + field + "' && rating < 3")
                .withOutFields(Collections.singletonList("count(*)"))
                .build());
        return "Broj loših rezultata u oblasti " + field + ": " + response.getData().toString();
    }

    //SLOŽENI UPIT: Vektorska pretraga + 2 filtera
    @GetMapping("/search/complex-filter")
    public String complexSearch(@RequestParam String query, @RequestParam String category) {
        List<Float> queryVector = embeddingService.generateEmbedding(query);
        
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .withMetricType(MetricType.L2)
                .withTopK(5)
                .withFloatVectors(Collections.singletonList(queryVector))
                .withVectorFieldName("prompt_vector")
                .withExpr("category == '" + category + "' && avg_rating > 3.5")
                .withOutFields(List.of("prompt_text", "avg_rating"))
                .build();

        return milvusClient.search(searchParam).getData().toString();
    }

    //SLOŽENI UPIT: Vektorska pretraga uz Iterator
  @GetMapping("/search/iterator")
public List<String> searchWithIterator(@RequestParam String query) {
    List<String> results = new ArrayList<>();
    try {
        List<Float> queryVector = embeddingService.generateEmbedding(query);

        SearchIteratorParam iteratorParam = SearchIteratorParam.newBuilder()
                .withCollectionName("ResearcherFeedbacks")
                .withVectorFieldName("feedback_vector")
                .withFloatVectors(Collections.singletonList(queryVector))
                .withBatchSize(10L)
                .withExpr("regeneration_count >= 0")
                .withMetricType(MetricType.L2)
                .withTopK(50)
                // OVO JE FALILO: Moramo reći Milvusu da nam vrati i tekst komentara
                .withOutFields(Collections.singletonList("feedback_comment"))
                .build();

        R<SearchIterator> response = milvusClient.searchIterator(iteratorParam);

        if (response.getStatus() != 0) {
            results.add("Milvus greška: " + response.getMessage());
            return results;
        }

        SearchIterator iterator = response.getData();
        if (iterator == null) {
            results.add("Greška: Iterator je null");
            return results;
        }

        for (int i = 0; i < 2; i++) {
            List<QueryResultsWrapper.RowRecord> batch = iterator.next();
            if (batch == null || batch.isEmpty()) break;
            
            for (QueryResultsWrapper.RowRecord record : batch) {
                // Sada će polje postojati u rezultatu
                Object comment = record.get("feedback_comment");
                results.add(comment != null ? comment.toString() : "Komentar je prazan");
            }
        }

    } catch (Exception e) {
        results.add("Sistemska greška: " + e.getMessage());
        e.printStackTrace();
    }
    return results;
}

    //SLOŽENI UPIT: HIBRIDNA PRETRAGA (Multi-Vector + Ranker)
    @GetMapping("/search/hybrid")
    public String hybridSearch(@RequestParam String query) {
        List<Float> queryVector = embeddingService.generateEmbedding(query);

        AnnSearchParam req1 = AnnSearchParam.newBuilder()
                .withVectorFieldName("prompt_vector")
                .withFloatVectors(Collections.singletonList(queryVector))
                .withMetricType(MetricType.L2)
                .withTopK(10)
                .build();

        AnnSearchParam req2 = AnnSearchParam.newBuilder()
                .withVectorFieldName("summary_vector")
                .withFloatVectors(Collections.singletonList(queryVector))
                .withMetricType(MetricType.L2)
                .withTopK(10)
                .build();

        HybridSearchParam hybridSearchParam = HybridSearchParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .addSearchRequest(req1)
                .addSearchRequest(req2)
                .withRanker(RRFRanker.newBuilder().withK(60).build())
                .withTopK(5)
                .withOutFields(List.of("prompt_text", "short_summary"))
                .build();

        return milvusClient.hybridSearch(hybridSearchParam).getData().toString();
    }
}