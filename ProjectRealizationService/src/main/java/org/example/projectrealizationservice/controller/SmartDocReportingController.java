package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.service.SmartDocReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/smart-docs/reports")
@RequiredArgsConstructor
public class SmartDocReportingController {

    private final SmartDocReportingService reportingService;

    @GetMapping("/full-report")
    public ResponseEntity<Map<String, Object>> getFullReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("researcherActivity", reportingService.getResearcherActivity());
        
        report.put("templatePerformance", reportingService.getComplexTemplateAnalysis());
        
        report.put("fieldRatings", reportingService.getRatingStats());
        
        return ResponseEntity.ok(report);
    }
}