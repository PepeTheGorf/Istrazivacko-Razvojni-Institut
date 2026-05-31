package org.example.projectrealizationanalyticsservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projectrealizationanalyticsservice.analytics.AnalyticsMeasurements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfluxDataSeederService {

    private static final String[] PHASES = {"BACKLOG", "IN_PROGRESS", "REVIEW", "DONE"};
    private static final int WRITE_BATCH_SIZE = 500;
    private static final int SEED_HISTORY_DAYS = 365;

    private final InfluxDBClient influxDBClient;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String influxOrg;

    private final Random random = new Random(42);

    public void seedPhaseTransitionEvents(int recordCount) {
        if (recordCount <= 0) {
            return;
        }

        Instant windowStart = Instant.now().minus(SEED_HISTORY_DAYS, ChronoUnit.DAYS);
        long windowSeconds = SEED_HISTORY_DAYS * 24L * 3600L;
        List<Point> batch = new ArrayList<>(WRITE_BATCH_SIZE);

        for (int i = 0; i < recordCount; i++) {
            int fromIndex = random.nextInt(PHASES.length);
            int toIndex = random.nextInt(PHASES.length);
            while (toIndex == fromIndex) {
                toIndex = random.nextInt(PHASES.length);
            }

            Instant timestamp = windowStart
                    .plusSeconds(random.nextLong(windowSeconds))
                    .plusMillis(random.nextInt(1000));

            Point point = Point
                    .measurement(AnalyticsMeasurements.TASK_PHASE_TRANSITIONS)
                    .addTag("projectName", "project-" + (random.nextInt(25) + 1))
                    .addTag("fromPhase", PHASES[fromIndex])
                    .addTag("toPhase", PHASES[toIndex])
                    .addTag("taskId", "task-" + (random.nextInt(120) + 1))
                    .addTag("userId", String.valueOf(random.nextInt(50) + 1))
                    .addField("durationSeconds", 300L + random.nextInt(86_400))
                    .time(timestamp, WritePrecision.MS);

            batch.add(point);

            if (batch.size() >= WRITE_BATCH_SIZE) {
                influxDBClient.getWriteApiBlocking().writePoints(bucket, influxOrg, batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            influxDBClient.getWriteApiBlocking().writePoints(bucket, influxOrg, batch);
        }

        log.info("Seeded {} records into '{}'", recordCount, AnalyticsMeasurements.TASK_PHASE_TRANSITIONS);
    }
}
