package org.example.promptvectorservice.service;

import io.milvus.client.MilvusServiceClient;
//import io.milvus.param.R;
import io.milvus.param.dml.InsertParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataSeederService {

    @Autowired
    private MilvusServiceClient milvusClient;

    private final Random random = new Random();

    public void seedData() {
        seedTemplates();
        seedFeedbacks();
    }

    private void seedTemplates() {
        String[] categories = {"Medicina", "Tehnologija", "Ekonomija", "Ekologija", "Pravo"};
        String[] names = {"Uvod", "Metodologija", "Analiza podataka", "Diskusija", "Zaključak"};

         List<Long> ids = new ArrayList<>();
        List<String> templateNames = new ArrayList<>();
        List<String> categoryList = new ArrayList<>();
        List<Float> ratings = new ArrayList<>();
        List<Integer> versions = new ArrayList<>();
        List<List<Float>> vectors = new ArrayList<>();
        List<List<Float>> summaryVectors = new ArrayList<>();

        for (int i = 0; i < 210; i++) {
            ids.add((long) i + 1);
            templateNames.add(names[random.nextInt(names.length)] + " - Verzija " + (i + 1));
            categoryList.add(categories[random.nextInt(categories.length)]);
            ratings.add(1.0f + random.nextFloat() * 4.0f);
            versions.add(random.nextInt(5) + 1);
            vectors.add(generateRandomVector(128));
            summaryVectors.add(generateRandomVector(128));
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", ids));
        fields.add(new InsertParam.Field("template_name", templateNames));
        fields.add(new InsertParam.Field("category", categoryList));
        fields.add(new InsertParam.Field("avg_rating", ratings));
        fields.add(new InsertParam.Field("version", versions));
        fields.add(new InsertParam.Field("prompt_vector", vectors));
         fields.add(new InsertParam.Field("summary_vector", summaryVectors));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("PromptTemplates")
                .withFields(fields)
                .build();

        milvusClient.insert(insertParam);
        System.out.println("Ubaceno 210 sablona u Milvus!");
    }

    private void seedFeedbacks() {
        String[] sections = {"Uvod", "Metodologija", "Rezultati", "Zaključak"};
        List<Long> ids = new ArrayList<>();
        List<Integer> ratings = new ArrayList<>();
        List<Integer> regens = new ArrayList<>();
        List<String> sectionList = new ArrayList<>();
        List<List<Float>> vectors = new ArrayList<>();

        for (int i = 0; i < 210; i++) {
            ids.add((long) i + 1000);
            ratings.add(random.nextInt(5) + 1);
            regens.add(random.nextInt(10));
            sectionList.add(sections[random.nextInt(sections.length)]);
            vectors.add(generateRandomVector(128));
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", ids));
        fields.add(new InsertParam.Field("rating", ratings));
        fields.add(new InsertParam.Field("regeneration_count", regens));
        fields.add(new InsertParam.Field("section_name", sectionList));
        fields.add(new InsertParam.Field("feedback_vector", vectors));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("ResearcherFeedbacks")
                .withFields(fields)
                .build();

        milvusClient.insert(insertParam);
        System.out.println("Ubačeno 210 feedback-ova u Milvus!");
    }

    private List<Float> generateRandomVector(int dimension) {
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextFloat());
        }
        return vector;
    }
}