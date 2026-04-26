package org.example.promptvectorservice.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CollectionSchemaParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import io.milvus.param.collection.HasCollectionParam;
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

    private static final String TEMPLATE_COLLECTION = "PromptTemplates";
    private static final String FEEDBACK_COLLECTION = "ResearcherFeedbacks";

    @Autowired
    private DataSeederService dataSeederService;

    @PostConstruct
    public void init() {
        createPromptTemplatesCollection();
        createResearcherFeedbackCollection();

        milvusClient.loadCollection(io.milvus.param.collection.LoadCollectionParam.newBuilder()
                .withCollectionName(TEMPLATE_COLLECTION).build());
        milvusClient.loadCollection(io.milvus.param.collection.LoadCollectionParam.newBuilder()
                .withCollectionName(FEEDBACK_COLLECTION).build());

        if (getCollectionCount(TEMPLATE_COLLECTION) == 0) {
            dataSeederService.seedData();
            System.out.println("Podaci su uspešno generisani!");
        } else {
            System.out.println("Podaci već postoje u bazi, preskačem seeding.");
        }
    }

    private void createPromptTemplatesCollection() {
        if (!collectionExists(TEMPLATE_COLLECTION)) {
            List<FieldType> fields = new ArrayList<>();
            fields.add(FieldType.newBuilder().withName("id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(true).build());
            fields.add(FieldType.newBuilder().withName("template_name").withDataType(DataType.VarChar).withMaxLength(255).build());
            fields.add(FieldType.newBuilder().withName("category").withDataType(DataType.VarChar).withMaxLength(100).build());
            fields.add(FieldType.newBuilder().withName("avg_rating").withDataType(DataType.Float).build());
            fields.add(FieldType.newBuilder().withName("version").withDataType(DataType.Int32).build());
            fields.add(FieldType.newBuilder().withName("prompt_vector").withDataType(DataType.FloatVector).withDimension(128).build());

            CollectionSchemaParam schemaParam = CollectionSchemaParam.newBuilder()
                    .withFieldTypes(fields)
                    .build();

            milvusClient.createCollection(CreateCollectionParam.newBuilder()
                    .withCollectionName(TEMPLATE_COLLECTION)
                    .withSchema(schemaParam) 
                    .build());
            
            createIndex(TEMPLATE_COLLECTION, "prompt_vector");
        }
    }

    private void createResearcherFeedbackCollection() {
        if (!collectionExists(FEEDBACK_COLLECTION)) {
            List<FieldType> fields = new ArrayList<>();
            fields.add(FieldType.newBuilder().withName("id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(true).build());
            fields.add(FieldType.newBuilder().withName("rating").withDataType(DataType.Int32).build());
            fields.add(FieldType.newBuilder().withName("regeneration_count").withDataType(DataType.Int32).build());
            fields.add(FieldType.newBuilder().withName("section_name").withDataType(DataType.VarChar).withMaxLength(100).build());
            fields.add(FieldType.newBuilder().withName("feedback_vector").withDataType(DataType.FloatVector).withDimension(128).build());

            CollectionSchemaParam schemaParam = CollectionSchemaParam.newBuilder()
                    .withFieldTypes(fields)
                    .build();

            milvusClient.createCollection(CreateCollectionParam.newBuilder()
                    .withCollectionName(FEEDBACK_COLLECTION)
                    .withSchema(schemaParam)
                    .build());

            createIndex(FEEDBACK_COLLECTION, "feedback_vector");
        }
    }

    private boolean collectionExists(String name) {
        return milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(name).build()).getData();
    }

    private void createIndex(String collectionName, String fieldName) {
        milvusClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(fieldName)
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.L2)
                .withExtraParam("{\"nlist\":1024}")
                .build());
    }
     private long getCollectionCount(String collectionName) {
        try {
            R<GetCollectionStatisticsResponse> response = milvusClient.getCollectionStatistics(
                    GetCollectionStatisticsParam.newBuilder().withCollectionName(collectionName).build());
            return response.getData().getStatsList().stream()
                    .filter(stat -> stat.getKey().equals("row_count"))
                    .map(stat -> Long.parseLong(stat.getValue()))
                    .findFirst().orElse(0L);
        } catch (Exception e) {
            return 0;
        }
    }
}