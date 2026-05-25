package org.example.promptvectorservice.service;

import io.milvus.client.MilvusServiceClient;
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

    @Autowired
    private EmbeddingService embeddingService;

    private final Random random = new Random();

    private final String[] domains = {"Astrophysics", "Bio-engineering", "Macroeconomics", "Digital Ethics", "Quantum Mechanics", "Neuroscience", "Ancient History", "Climatology", "Cybersecurity", "Linguistics", "Pharmacology", "Robotics", "Urban Sociology", "Renewable Energy"};
    private final String[] roles = {"Senior Researcher", "PhD Candidate", "Journal Editor", "Data Analyst", "Science Communicator", "Ethical Consultant"};
    private final String[] methods = {"Double-blind study", "Comparative analysis", "Statistical modeling", "Literature review", "Qualitative interview", "Simulation-based test"};
    private final String[] goals = {"submission to Nature Journal", "a global conference presentation", "securing private funding", "educational purposes", "cross-disciplinary integration"};
    private final String[] constraints = {"within strict ethical bounds", "considering budgetary limits", "ignoring outdated 20th-century theories", "using real-time sensor data"};

    private final String[] expertTerms = {"methodological rigor", "statistical significance", "peer-review standards", "citation accuracy"};
    private final String[] beginnerTerms = {"clarity of explanation", "ease of use", "helpful examples", "concise language"};
    private final String[] skepticTerms = {"hallucination issues", "logical fallacies", "over-simplified conclusions", "redundant output"};

    public void seedData() {
        System.out.println("Započinjem napredni seeding (20.000 unikatnih zapisa)...");
        seedTemplatesInBatches(10000, 500);
        seedFeedbacksInBatches(10000, 500);
        System.out.println("Seeding završen uspešno!");
    }

    private void seedTemplatesInBatches(int total, int batchSize) {
        for (int i = 0; i < total; i += batchSize) {
            List<Long> ids = new ArrayList<>();
            List<String> categories = new ArrayList<>();
            List<Float> ratings = new ArrayList<>();
            List<Integer> versions = new ArrayList<>();
            List<String> texts = new ArrayList<>();
            List<String> summaries = new ArrayList<>();
            List<List<Float>> promptVectors = new ArrayList<>();
            List<List<Float>> summaryVectors = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                String d1 = domains[random.nextInt(domains.length)];
                String d2 = domains[random.nextInt(domains.length)];
                String role = roles[random.nextInt(roles.length)];
                String method = methods[random.nextInt(methods.length)];
                String goal = goals[random.nextInt(goals.length)];
                String constraint = constraints[random.nextInt(constraints.length)];

                String fullPrompt = generateAdvancedPrompt(role, d1, d2, method, goal, constraint);
                String shortSum = d1 + (d1.equals(d2) ? "" : " & " + d2) + " Analysis";

                ids.add((long) i + j + 1);
                categories.add(d1); 
                ratings.add(1.0f + random.nextFloat() * 4.0f);
                versions.add(random.nextInt(10) + 1);
                texts.add(fullPrompt);
                summaries.add(shortSum);
                
                promptVectors.add(embeddingService.generateEmbedding(fullPrompt));
                summaryVectors.add(embeddingService.generateEmbedding(shortSum));
            }
            insertBatch("PromptTemplates", ids, categories, ratings, versions, texts, summaries, promptVectors, summaryVectors);
            System.out.println("[Templates] Batch završen: " + (i + batchSize));
        }
    }

    private String generateAdvancedPrompt(String role, String d1, String d2, String method, String goal, String constraint) {
        int blueprint = random.nextInt(4);
        return switch (blueprint) {
            case 0 -> String.format("As a %s, perform a %s on the intersection of %s and %s for %s.", role, method, d1, d2, goal);
            case 1 -> String.format("Develop a research proposal that bridges %s and %s, %s, aimed at %s.", d1, d2, constraint, goal);
            case 2 -> String.format("Critically evaluate the impact of %s on modern %s using a %s approach.", d1, d2, method);
            default -> String.format("Summarize key findings in %s and %s, %s, for a %s audience.", d1, d2, constraint, role);
        };
    }

    private void seedFeedbacksInBatches(int total, int batchSize) {
        for (int i = 0; i < total; i += batchSize) {
            List<Long> ids = new ArrayList<>();
            List<Integer> ratings = new ArrayList<>();
            List<Integer> regens = new ArrayList<>();
            List<String> fieldsList = new ArrayList<>();
            List<String> comments = new ArrayList<>();
            List<List<Float>> vectors = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                String personaComment = generatePersonaFeedback();
                String field = domains[random.nextInt(domains.length)];

                ids.add((long) i + j + 50000); 
                ratings.add(random.nextInt(5) + 1);
                regens.add(random.nextInt(15));
                fieldsList.add(field);
                comments.add(personaComment + " (Field: " + field + ")");
                vectors.add(embeddingService.generateEmbedding(personaComment));
            }
            insertFeedbackBatch(ids, ratings, regens, fieldsList, comments, vectors);
            System.out.println("[Feedbacks] Batch završen: " + (i + batchSize));
        }
    }

    private String generatePersonaFeedback() {
        int type = random.nextInt(3);
        return switch (type) {
            case 0 -> "Expert review: The " + expertTerms[random.nextInt(expertTerms.length)] + " is impressive, but needs more depth.";
            case 1 -> "Student feedback: I loved the " + beginnerTerms[random.nextInt(beginnerTerms.length)] + ", it made the topic much easier.";
            default -> "System alert: Encountered " + skepticTerms[random.nextInt(skepticTerms.length)] + " during the last generation.";
        };
    }

    private void insertBatch(String coll, List<Long> ids, List<String> cats, List<Float> rats, List<Integer> vers, List<String> texts, List<String> sums, List<List<Float>> pVecs, List<List<Float>> sVecs) {
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", ids));
        fields.add(new InsertParam.Field("category", cats));
        fields.add(new InsertParam.Field("avg_rating", rats));
        fields.add(new InsertParam.Field("version", vers));
        fields.add(new InsertParam.Field("prompt_text", texts));
        fields.add(new InsertParam.Field("short_summary", sums));
        fields.add(new InsertParam.Field("prompt_vector", pVecs));
        fields.add(new InsertParam.Field("summary_vector", sVecs));

        milvusClient.insert(InsertParam.newBuilder().withCollectionName(coll).withFields(fields).build());
    }

    private void insertFeedbackBatch(List<Long> ids, List<Integer> rats, List<Integer> regs, List<String> fieldsL, List<String> comments, List<List<Float>> vecs) {
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", ids));
        fields.add(new InsertParam.Field("rating", rats));
        fields.add(new InsertParam.Field("regeneration_count", regs));
        fields.add(new InsertParam.Field("research_field", fieldsL));
        fields.add(new InsertParam.Field("feedback_comment", comments));
        fields.add(new InsertParam.Field("feedback_vector", vecs));

        milvusClient.insert(InsertParam.newBuilder().withCollectionName("ResearcherFeedbacks").withFields(fields).build());
    }
}