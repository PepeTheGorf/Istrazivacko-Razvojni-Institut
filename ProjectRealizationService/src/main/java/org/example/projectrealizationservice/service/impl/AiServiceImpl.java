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
    public String generateText(String systemPrompt, String context, String userInput) {
        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        String fullUserPrompt = String.format(
            "Kontekst prethodnih sekcija:\n%s\n\nSpecifičan unos/instrukcija korisnika za ovu sekciju:\n%s",
            context != null && !context.isBlank() ? context : "Nema prethodnog konteksta.",
            userInput
        );
        UserMessage userMessage = new UserMessage(fullUserPrompt);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        
        try {
            return chatModel.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            return "Greška prilikom generisanja sadržaja: " + e.getMessage();
        }
    }
}