package org.example.projectrealizationservice.service.impl;

import org.springframework.context.annotation.Primary;
import org.example.projectrealizationservice.service.AiService;
import org.springframework.stereotype.Service;

@Service
@Primary 
public class MockAiService implements AiService {

    @Override
    public String generateText(String systemPrompt, String context, String userInput) {
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