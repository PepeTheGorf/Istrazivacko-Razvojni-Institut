package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.service.AiService;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.service.mode", havingValue = "real")
public class AiServiceImpl implements AiService {

    private final ChatModel chatModel;

    @Override
    public String generateText(String systemPrompt, String context, String userInput, String currentDraft) {
        
        String finalSystemPrompt = "STRICT INSTRUCTIONS: " +
            "1. Output ONLY the raw content for the document section. " +
            "2. DO NOT include introductions like 'Here is...', 'Based on...', or 'Sure!'. " +
            "3. DO NOT include conclusions like 'I hope this helps' or 'Let me know...'. " +
            "4. NEVER mention that you are an AI or explain your process. " +
            "5. If the user provides a draft, output ONLY the improved version of that text.\n\n" +
            "CORE GUIDELINES:\n" + systemPrompt;

        SystemMessage systemMessage = new SystemMessage(finalSystemPrompt);

        StringBuilder userPromptBuilder = new StringBuilder();
        
        if (context != null && !context.isBlank()) {
            userPromptBuilder.append("### CONTEXT FROM PREVIOUS SECTIONS:\n").append(context).append("\n\n");
        }

        userPromptBuilder.append("### USER DATA/INPUT:\n").append(userInput).append("\n\n");

        if (currentDraft != null && !currentDraft.isBlank()) {
            userPromptBuilder.append("### CURRENT DRAFT TO REFINE:\n")
                             .append(currentDraft)
                             .append("\n\nACTION: Improve the current draft using the provided user data and context.");
        } else {
            userPromptBuilder.append("ACTION: Write a new document section based on the user data and context.");
        }

        UserMessage userMessage = new UserMessage(userPromptBuilder.toString());
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        
        try {
            String result = chatModel.call(prompt).getResult().getOutput().getContent();
            
            return cleanAiResponse(result);
        } catch (Exception e) {
            return "Greška prilikom generisanja sadržaja: " + e.getMessage();
        }
    }

    private String cleanAiResponse(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)^Here is (the )?generated content:.*", "")
                   .replaceAll("(?i)^Sure, here is.*", "")
                   .replaceAll("(?i)Let me know if this meets your expectations!.*$", "")
                   .replaceAll("(?i)I hope this helps!.*$", "")
                   .trim();
    }
}