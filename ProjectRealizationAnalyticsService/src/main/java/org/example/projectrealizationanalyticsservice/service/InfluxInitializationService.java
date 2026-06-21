package org.example.projectrealizationanalyticsservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfluxInitializationService {

    public static final boolean shouldSeed = true;
    public static final int seedRecordCount = 10000;

    private final InfluxDataSeederService dataSeederService;

    @PostConstruct
    public void initialize() {
        if (!shouldSeed) {
            return;
        }
        log.info("InfluxDB seeding enabled — writing {} records", seedRecordCount);
        dataSeederService.seedPhaseTransitionEvents(seedRecordCount);
    }
}
