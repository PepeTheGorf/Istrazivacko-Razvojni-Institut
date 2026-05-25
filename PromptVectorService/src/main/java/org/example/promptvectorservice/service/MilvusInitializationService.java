package org.example.promptvectorservice.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.*;
import io.milvus.param.index.CreateIndexParam;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MilvusInitializationService {

    @Autowired
    private MilvusServiceClient milvusClient;

    @Autowired
    private DataSeederService dataSeederService;

    public static final String TEMPLATE_COLLECTION = "PromptTemplates";
    public static final String FEEDBACK_COLLECTION = "ResearcherFeedbacks";
    private static final Integer VECTOR_DIM = 384;

    @PostConstruct
    public void init() {
        createPromptTemplatesCollection();
        createResearcherFeedbackCollection();

        milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(TEMPLATE_COLLECTION).build());
        milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(FEEDBACK_COLLECTION).build());

        if (getCollectionCount(TEMPLATE_COLLECTION) < 10000) {
            dataSeederService.seedData();
        } else {
            System.out.println("Podaci već postoje (10.000+), preskačem seeding.");
        }
    }

    private void createPromptTemplatesCollection() {
        if (!collectionExists(TEMPLATE_COLLECTION)) {
            List<FieldType> fields = new ArrayList<>();
            fields.add(FieldType.newBuilder().withName("id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(false).build());
            fields.add(FieldType.newBuilder().withName("category").withDataType(DataType.VarChar).withMaxLength(100).build());
            fields.add(FieldType.newBuilder().withName("avg_rating").withDataType(DataType.Float).build());
            fields.add(FieldType.newBuilder().withName("version").withDataType(DataType.Int32).build());
            fields.add(FieldType.newBuilder().withName("prompt_text").withDataType(DataType.VarChar).withMaxLength(1000).build());
            fields.add(FieldType.newBuilder().withName("short_summary").withDataType(DataType.VarChar).withMaxLength(255).build());
            fields.add(FieldType.newBuilder().withName("prompt_vector").withDataType(DataType.FloatVector).withDimension(VECTOR_DIM).build());
            fields.add(FieldType.newBuilder().withName("summary_vector").withDataType(DataType.FloatVector).withDimension(VECTOR_DIM).build());

            milvusClient.createCollection(CreateCollectionParam.newBuilder()
                    .withCollectionName(TEMPLATE_COLLECTION)
                    .withSchema(CollectionSchemaParam.newBuilder().withFieldTypes(fields).build())
                    .build());

            createHNSWIndex(TEMPLATE_COLLECTION, "prompt_vector");
            createHNSWIndex(TEMPLATE_COLLECTION, "summary_vector");
        }
    }

    private void createResearcherFeedbackCollection() {
        if (!collectionExists(FEEDBACK_COLLECTION)) {
            List<FieldType> fields = new ArrayList<>();
            fields.add(FieldType.newBuilder().withName("id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(false).build());
            fields.add(FieldType.newBuilder().withName("rating").withDataType(DataType.Int32).build());
            fields.add(FieldType.newBuilder().withName("regeneration_count").withDataType(DataType.Int32).build());
            fields.add(FieldType.newBuilder().withName("research_field").withDataType(DataType.VarChar).withMaxLength(100).build());
            fields.add(FieldType.newBuilder().withName("feedback_comment").withDataType(DataType.VarChar).withMaxLength(1000).build());
            fields.add(FieldType.newBuilder().withName("feedback_vector").withDataType(DataType.FloatVector).withDimension(VECTOR_DIM).build());

            milvusClient.createCollection(CreateCollectionParam.newBuilder()
                    .withCollectionName(FEEDBACK_COLLECTION)
                    .withSchema(CollectionSchemaParam.newBuilder().withFieldTypes(fields).build())
                    .build());

            createHNSWIndex(FEEDBACK_COLLECTION, "feedback_vector");
        }
    }

    private void createHNSWIndex(String collectionName, String fieldName) {
        milvusClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(fieldName)
                .withIndexType(IndexType.HNSW)
                .withMetricType(MetricType.L2)
                .withExtraParam("{\"M\":16, \"efConstruction\":64}")
                .build());
    }

    private boolean collectionExists(String name) {
        return milvusClient.hasCollection(HasCollectionParam.newBuilder().withCollectionName(name).build()).getData();
    }

    private long getCollectionCount(String collectionName) {
        try {
            R<GetCollectionStatisticsResponse> response = milvusClient.getCollectionStatistics(
                    GetCollectionStatisticsParam.newBuilder().withCollectionName(collectionName).build());
            return response.getData().getStatsList().stream()
                    .filter(stat -> stat.getKey().equals("row_count"))
                    .map(stat -> Long.parseLong(stat.getValue()))
                    .findFirst().orElse(0L);
        } catch (Exception e) { return 0; }
    }
}