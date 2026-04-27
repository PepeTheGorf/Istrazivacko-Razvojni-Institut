package org.example.promptvectorservice.service;

import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    private static final int EMBEDDING_DIMENSION = 384;
    private final EmbeddingModel embeddingModel;

    public EmbeddingService() {
        this.embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
    }

    public List<Float> generateEmbedding(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return generateZeroEmbedding();
            }

            var response = embeddingModel.embed(text);
            float[] embedding = response.content().vector();

            List<Float> result = new java.util.ArrayList<>();
            for (float value : embedding) {
                result.add(value);
            }
            return result;

        } catch (Exception e) {
            System.err.println("Error generating embedding: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    public int getEmbeddingDimension() {
        return EMBEDDING_DIMENSION;
    }

    private List<Float> generateZeroEmbedding() {
        return java.util.Collections.nCopies(EMBEDDING_DIMENSION, 0.0f);
    }
}
