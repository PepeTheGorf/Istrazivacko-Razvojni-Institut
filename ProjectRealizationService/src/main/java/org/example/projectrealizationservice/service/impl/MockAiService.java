package org.example.projectrealizationservice.service.impl;

import org.springframework.context.annotation.Primary;
import org.example.projectrealizationservice.service.AiService;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
//@Primary, kad nisam dodao ovu dole anotaciju ovo je bilo
@ConditionalOnProperty(name = "ai.service.mode", havingValue = "mock")
public class MockAiService implements AiService {

    @Override
    public String generateText(String systemPrompt, String context, String userInput, String currentDraft) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (userInput == null || userInput.isBlank()) {
            return "Greška: Niste uneli nikakve podatke za generisanje ove sekcije.";
        }

        return "[AI GENERISANO]: Na osnovu vaših beleški ('" + userInput + "') " +
               "i prateći sistemska uputstva, generisan je ovaj profesionalni tekst. " +
               "\n\nKontekst dokumenta je uzet u obzir kako bi se osigurala koherentnost sa prethodnim delovima.";
    }
}