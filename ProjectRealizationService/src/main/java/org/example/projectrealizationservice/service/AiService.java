package org.example.projectrealizationservice.service;

public interface AiService {
    String generateText(String systemPrompt, String context, String userInput, String currentDraft);
}