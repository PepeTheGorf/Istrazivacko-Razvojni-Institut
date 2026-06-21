package org.example.projectrealizationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.client.CassandraClient;
import org.example.projectrealizationservice.dto.smartdocs.SmartTemplateDTO;
import org.example.projectrealizationservice.repository.sql.smartdocs.SmartTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartDocReportingService {

    private final SmartTemplateRepository templateRepository;
    private final CassandraClient cassandraClient;

    public List<Map<String, Object>> getComplexTemplateAnalysis() {
        return templateRepository.findAll().stream().map(template -> {
            Map<String, Object> map = new HashMap<>();
            map.put("templateName", template.getName());
            map.put("category", template.getCategory().getName());
            
            List<Float> effectivenessScores = cassandraClient.getTemplateEffectiveness(template.getId().toString());
            
            double avgEffectiveness = effectivenessScores.stream()
                    .mapToDouble(Float::doubleValue)
                    .average()
                    .orElse(0.0);
            
            map.put("averageEffectiveness", Math.round(avgEffectiveness * 100.0) / 100.0);
            map.put("usageCount", effectivenessScores.size());
            
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Long> getResearcherActivity() {
        return cassandraClient.getRequestsCount();
    }

    public Map<String, Double> getRatingStats() {
        return cassandraClient.getAvgRating();
    }
}