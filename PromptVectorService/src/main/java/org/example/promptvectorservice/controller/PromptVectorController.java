package org.example.promptvectorservice.controller;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
//import io.milvus.grpc.MutationResult;
import io.milvus.grpc.QueryResults;
//import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.dml.*;
//import io.milvus.v2.service.vector.request.ranker.RRFRanker;
//import io.milvus.param.dml.rank.RRFRanker; 
//import io.milvus.param.dml.rank.WeightedRanker;
//import io.milvus.param.dml.RRFRanker;
import io.milvus.orm.iterator.SearchIterator;
import io.milvus.response.QueryResultsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/vector")
public class PromptVectorController {

    @Autowired
    private MilvusServiceClient milvusClient;

    private final Random random = new Random();
    //CREATE
    @PostMapping("/template")
    public String createTemplate(@RequestParam String name, @RequestParam String category) {
        insertData("PromptTemplates", "template_name", List.of(name), "category", List.of(category), "prompt_vector");
        return "Sablon kreiran!";
    }
    @PostMapping("/feedback")
    public String createFeedback(@RequestParam Integer rating, @RequestParam String section) {
        insertFeedbackData(rating, section);
        return "Feedback kreiran!";
    }
    //READ
    @GetMapping("/template/{id}")
    public String getTemplate(@PathVariable Long id) {
        return queryById("PromptTemplates", id, List.of("template_name", "category", "avg_rating"));
    }
    @GetMapping("/feedback/{id}")
    public String getFeedback(@PathVariable Long id) {
        return queryById("ResearcherFeedbacks", id, List.of("section_name", "rating"));
    }
    //UPDATE
    /*@PutMapping("/template/{id}")
    public String updateTemplate(@PathVariable Long id, @RequestParam Float rating) {
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", Collections.singletonList(id)));
        fields.add(new InsertParam.Field("avg_rating", Collections.singletonList(rating)));
        milvusClient.upsert(UpsertParam.newBuilder().withCollectionName("PromptTemplates").withFields(fields).build());
        return "Sablon azuriran!";
    }*/
   @PutMapping("/template/{id}")
   public String updateTemplate(@PathVariable Long id, @RequestParam Float rating) {
    R<QueryResults> queryResponse = milvusClient.query(QueryParam.newBuilder()
            .withCollectionName("PromptTemplates")
            .withExpr("id == " + id)
            .withOutFields(List.of("template_name", "category", "version", "prompt_vector", "summary_vector"))
            .build());

    QueryResultsWrapper wrapper = new QueryResultsWrapper(queryResponse.getData());
    List<QueryResultsWrapper.RowRecord> records = wrapper.getRowRecords();

    if (records.isEmpty()) return "Greška: Šablon sa ID " + id + " nije pronađen.";

    QueryResultsWrapper.RowRecord existing = records.get(0);

    List<InsertParam.Field> fields = new ArrayList<>();
    fields.add(new InsertParam.Field("id", List.of(id)));
    fields.add(new InsertParam.Field("avg_rating", List.of(rating)));
    fields.add(new InsertParam.Field("template_name", List.of(existing.get("template_name"))));
    fields.add(new InsertParam.Field("category", List.of(existing.get("category"))));
    fields.add(new InsertParam.Field("version", List.of(existing.get("version"))));
    fields.add(new InsertParam.Field("prompt_vector", List.of(existing.get("prompt_vector"))));
    fields.add(new InsertParam.Field("summary_vector", List.of(existing.get("summary_vector"))));

    R<MutationResult> response = milvusClient.upsert(UpsertParam.newBuilder()
            .withCollectionName("PromptTemplates")
            .withFields(fields)
            .build());

    return response.getStatus() == 0 ? "Šablon uspešno ažuriran!" : "Greška: " + response.getMessage();
}
    /*@PutMapping("/feedback/{id}")
    public String updateFeedback(@PathVariable Long id, @RequestParam Integer rating) {
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", Collections.singletonList(id)));
        fields.add(new InsertParam.Field("rating", Collections.singletonList(rating)));
        milvusClient.upsert(UpsertParam.newBuilder().withCollectionName("ResearcherFeedbacks").withFields(fields).build());
        return "Feedback azuriran!";
    }*/
   @PutMapping("/feedback/{id}")
   public String updateFeedback(@PathVariable Long id, @RequestParam Integer rating) {
    R<QueryResults> queryResponse = milvusClient.query(QueryParam.newBuilder()
            .withCollectionName("ResearcherFeedbacks")
            .withExpr("id == " + id)
            .withOutFields(List.of("section_name", "regeneration_count", "feedback_vector"))
            .build());

    QueryResultsWrapper wrapper = new QueryResultsWrapper(queryResponse.getData());
    List<QueryResultsWrapper.RowRecord> records = wrapper.getRowRecords();

    if (records.isEmpty()) return "Greška: Feedback nije pronađen.";

    QueryResultsWrapper.RowRecord existing = records.get(0);

    List<InsertParam.Field> fields = new ArrayList<>();
    fields.add(new InsertParam.Field("id", List.of(id)));
    fields.add(new InsertParam.Field("rating", List.of(rating)));
    fields.add(new InsertParam.Field("section_name", List.of(existing.get("section_name"))));
    fields.add(new InsertParam.Field("regeneration_count", List.of(existing.get("regeneration_count"))));
    fields.add(new InsertParam.Field("feedback_vector", List.of(existing.get("feedback_vector"))));

    R<MutationResult> response = milvusClient.upsert(UpsertParam.newBuilder()
            .withCollectionName("ResearcherFeedbacks")
            .withFields(fields)
            .build());

    return response.getStatus() == 0 ? "Feedback uspešno ažuriran!" : "Greška: " + response.getMessage();
}
    //DELETE
    @DeleteMapping("/template/{id}")
    public String deleteTemplate(@PathVariable Long id) {
        milvusClient.delete(DeleteParam.newBuilder().withCollectionName("PromptTemplates").withExpr("id == " + id).build());
        return "Sablon obrisan!";
    }
    @DeleteMapping("/feedback/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        milvusClient.delete(DeleteParam.newBuilder().withCollectionName("ResearcherFeedbacks").withExpr("id == " + id).build());
        return "Feedback obrisan!";
    }
    //PROSTI UPITI
    @GetMapping("/search/simple-vector")
    public String simpleVectorSearch() {
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .withMetricType(MetricType.L2)
                .withTopK(3)
                .withFloatVectors(Collections.singletonList(generateRandomVector(128)))
                .withVectorFieldName("prompt_vector")
                .build();
        return milvusClient.search(searchParam).getData().toString();
    }
    @GetMapping("/feedback/count-bad")
    public String countBadFeedbacks(@RequestParam String section) {
        R<QueryResults> response = milvusClient.query(QueryParam.newBuilder()
                .withCollectionName("ResearcherFeedbacks")
                .withExpr("section_name == '" + section + "' && rating < 3")
                .withOutFields(Collections.singletonList("count(*)"))
                .build());
        return "Broj losih rezultata: " + response.getData().toString();
    }

    //vektorska pretraga + 2 filtera (kategorija i ocjena)
    @GetMapping("/search/complex-filter")
    public String complexSearchWithFilters() {
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .withMetricType(MetricType.L2)
                .withTopK(5)
                .withFloatVectors(Collections.singletonList(generateRandomVector(128)))
                .withVectorFieldName("prompt_vector")
                .withExpr("category == 'Tehnologija' && avg_rating > 4.0")
                .build();
        return milvusClient.search(searchParam).getData().toString();
    }
    //vektorska pretraga + filtriranje uz koristenje iteratora
    @GetMapping("/search/complex-iterator")
    public List<String> complexSearchIterator() {
        List<String> results = new ArrayList<>();
        SearchIteratorParam iteratorParam = SearchIteratorParam.newBuilder()
                .withCollectionName("ResearcherFeedbacks")
                .withVectorFieldName("feedback_vector")
                .withFloatVectors(Collections.singletonList(generateRandomVector(128)))
                .withBatchSize(10L)
                .withExpr("rating < 3")
                .withMetricType(MetricType.L2)
                .withTopK(50)
                .build();

        R<SearchIterator> response = milvusClient.searchIterator(iteratorParam);
        SearchIterator iterator = response.getData();

        int batchCount = 0;
        while (batchCount < 2) {
            List<QueryResultsWrapper.RowRecord> batch = iterator.next();
            if (batch == null || batch.isEmpty()) break;
            results.add("Batch " + batchCount + " size: " + batch.size());
            batchCount++;
        }
        return results;
    }

    // Složeni 3: Vektorska pretraga uz korišćenje HIBRIDNE PRETRAGE (AnnSearch + Ranker)
    /*@GetMapping("/search/hybrid")
    public String hybridSearch() {
        AnnSearchParam annSearchParam = AnnSearchParam.newBuilder()
                .withVectorFieldName("prompt_vector")
                .withFloatVectors(Collections.singletonList(generateRandomVector(128)))
                .withMetricType(MetricType.L2)
                .withTopK(5)
                .withExpr("version > 1") 
                .build();

        HybridSearchParam hybridSearchParam = HybridSearchParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .addSearchRequest(annSearchParam)
                .withRanker(RRFRanker.newBuilder().withK(60).build())
                .withTopK(5)
                .build();

        return milvusClient.hybridSearch(hybridSearchParam).getData().toString();
    }*/

    //POMOCNE METODE
    /*private void insertData(String coll, String f1, List<String> v1, String f2, List<String> v2, String vecF) {
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(f1, v1));
        fields.add(new InsertParam.Field(f2, v2));
        fields.add(new InsertParam.Field(vecF, Collections.singletonList(generateRandomVector(128))));
        milvusClient.insert(InsertParam.newBuilder().withCollectionName(coll).withFields(fields).build());
    }*/
 private void insertData(String coll, String f1, List<String> v1, String f2, List<String> v2, String vecF) {
    long newId = System.currentTimeMillis() + random.nextInt(1000);
    List<InsertParam.Field> fields = new ArrayList<>();
    fields.add(new InsertParam.Field("id", List.of(newId)));
    fields.add(new InsertParam.Field(f1, v1));
    fields.add(new InsertParam.Field(f2, v2));
    fields.add(new InsertParam.Field("avg_rating", List.of(0.0f))); 
    fields.add(new InsertParam.Field("version", List.of(1)));        
    fields.add(new InsertParam.Field(vecF, List.of(generateRandomVector(128))));
    fields.add(new InsertParam.Field("summary_vector", List.of(generateRandomVector(128)))); 

    milvusClient.insert(InsertParam.newBuilder()
            .withCollectionName(coll)
            .withFields(fields)
            .build());
}

    private void insertFeedbackData(Integer rating, String section) {
        long newId = System.currentTimeMillis() + random.nextInt(1000);
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", List.of(newId)));
        fields.add(new InsertParam.Field("rating", Collections.singletonList(rating)));
        fields.add(new InsertParam.Field("section_name", Collections.singletonList(section)));
        fields.add(new InsertParam.Field("feedback_vector", Collections.singletonList(generateRandomVector(128))));
        fields.add(new InsertParam.Field("regeneration_count", Collections.singletonList(0)));
        milvusClient.insert(InsertParam.newBuilder().withCollectionName("ResearcherFeedbacks").withFields(fields).build());
    }

    private String queryById(String coll, Long id, List<String> outFields) {
        R<QueryResults> response = milvusClient.query(QueryParam.newBuilder()
                .withCollectionName(coll).withExpr("id == " + id).withOutFields(outFields).build());
        if (response.getData() == null) return "Nije pronađeno.";
        return response.getData().toString();
    }

    private List<Float> generateRandomVector(int dimension) {
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < dimension; i++) vector.add(random.nextFloat());
        return vector;
    }
}