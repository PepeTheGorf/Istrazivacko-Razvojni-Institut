package org.example.projectrealizationanalyticsservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.example.projectrealizationanalyticsservice.analytics.AnalyticsMeasurements;
import org.example.projectrealizationanalyticsservice.analytics.model.PhaseTransitionEvent;
import org.example.projectrealizationanalyticsservice.analytics.model.RangeType;
import org.example.projectrealizationanalyticsservice.analytics.model.TimeRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InfluxService {

    private final InfluxDBClient influxDBClient;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String influxOrg;

    public void logPhaseTransition(PhaseTransitionEvent phaseTransitionEvent) {
        Point point = Point
                .measurement(AnalyticsMeasurements.TASK_PHASE_TRANSITIONS)
                .addTag("projectName", phaseTransitionEvent.getProjectName())
                .addTag("fromPhase", phaseTransitionEvent.getFromPhase())
                .addTag("toPhase", phaseTransitionEvent.getToPhase())
                .addTag("taskId", phaseTransitionEvent.getTaskId())
                .addTag("userId", String.valueOf(phaseTransitionEvent.getUserId()))
                .addField("durationSeconds", phaseTransitionEvent.getDurationInPreviousPhase())
                .addField("storyPoints", phaseTransitionEvent.getStoryPoints())
                .time(Instant.now(), WritePrecision.S);
        influxDBClient.getWriteApiBlocking().writePoint(bucket, influxOrg, point);
    }

    @Cacheable(value = "analytics:phase-transitions", key = "#projectName + ':' + #rangeType")
    public List<PhaseTransitionEvent> getTransitionsBetweenPhases(String projectName, RangeType rangeType) {
        OffsetDateTime now = OffsetDateTime.now();
        TimeRange range = rangeType.toTimeRange(now);
        String fluxQuery = """
                from(bucket: "project-analytics")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["_measurement"] == "task_phase_transitions")
                |> filter(fn: (r) => r["_field"] == "durationSeconds")
                |> filter(fn: (r) => r["projectName"] == "%s")
                |> group(columns: ["fromPhase", "toPhase"])
                |> count()
                |> group(columns: [])
                |> sort(columns: ["_value"], desc: true)
                """.formatted(range.getFrom(), range.getTo(), projectName);
        return mapFluxRecords(influxDBClient.getQueryApi(), fluxQuery, projectName, false);
    }
    
    @Cacheable(value = "analytics:time-spent-by-task", key = "#projectName + ':' + #rangeType")
    public List<PhaseTransitionEvent> getTimeSpentByTask(String projectName, RangeType rangeType) {
        OffsetDateTime now = OffsetDateTime.now();
        TimeRange range = rangeType.toTimeRange(now);
        String fluxQuery = """
                from(bucket: "project-analytics")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["_measurement"] == "task_phase_transitions")
                |> filter(fn: (r) => r["_field"] == "durationSeconds")
                |> filter(fn: (r) => r["projectName"] == "%s")
                |> group(columns: ["taskId", "fromPhase", "toPhase"])
                |> sum(column: "_value")
                |> group(columns: [])
                |> sort(columns: ["_value"], desc: true)
                """.formatted(range.getFrom(), range.getTo(), projectName);
        return mapFluxRecords(influxDBClient.getQueryApi(), fluxQuery, projectName, false);
    }
    
    @Cacheable(value = "analytics:avg-phase-duration-window", key = "#projectName + ':' + #rangeType + ':' + #window")
    public List<PhaseTransitionEvent> getAverageDurationInPhasesByProjectOverTime(String projectName, RangeType rangeType, String window) {
        OffsetDateTime now = OffsetDateTime.now();
        TimeRange range = rangeType.toTimeRange(now);
        String fluxQuery = """
                from(bucket: "project-analytics")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["_measurement"] == "task_phase_transitions")
                |> filter(fn: (r) => r["_field"] == "durationSeconds")
                |> filter(fn: (r) => r["projectName"] == "%s")
                |> group(columns: ["fromPhase"])
                |> window(every: %s)
                |> mean(column: "_value")
                |> sort(columns: ["_time"], desc: false)
                """.formatted(range.getFrom(), range.getTo(), projectName, window);
        return mapFluxRecords(influxDBClient.getQueryApi(), fluxQuery, projectName, true);
    }
    
    
    private List<PhaseTransitionEvent> mapFluxRecords(QueryApi queryApi, String fluxQuery, String projectName, boolean includeWindowStart) {
        List<PhaseTransitionEvent> events = new ArrayList<>();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable table : tables) {
            for (FluxRecord fluxRecord : table.getRecords()) {
                PhaseTransitionEvent event = new PhaseTransitionEvent();
                event.setProjectName(firstNonBlank(asString(fluxRecord.getValueByKey("projectName")), projectName));
                event.setFromPhase(asString(fluxRecord.getValueByKey("fromPhase")));
                event.setToPhase(asString(fluxRecord.getValueByKey("toPhase")));
                event.setTaskId(asString(fluxRecord.getValueByKey("taskId")));

                String userId = asString(fluxRecord.getValueByKey("userId"));
                if (userId != null) {
                    event.setUserId(Long.parseLong(userId));
                }

                Number duration = firstNumber(
                        fluxRecord.getValueByKey("durationSeconds"),
                        fluxRecord.getValueByKey("_value"),
                        fluxRecord.getValue()
                );
                event.setDurationInPreviousPhase(duration != null ? duration.longValue() : 0L);
                if (includeWindowStart) {
                    event.setWindowStart(resolveWindowStart(fluxRecord));
                }
                events.add(event);
            }
        }
        return events;
    }

    private static Instant resolveWindowStart(FluxRecord fluxRecord) {
        if (fluxRecord.getStart() != null) {
            return fluxRecord.getStart();
        }
        if (fluxRecord.getTime() != null) {
            return fluxRecord.getTime();
        }
        Instant fromStart = asInstant(fluxRecord.getValueByKey("_start"));
        if (fromStart != null) {
            return fromStart;
        }
        return asInstant(fluxRecord.getValueByKey("_time"));
    }

    private static Instant asInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof java.time.ZonedDateTime zonedDateTime) {
            return zonedDateTime.toInstant();
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    private static Number firstNumber(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate instanceof Number number) {
                return number;
            }
        }
        return null;
    }
}
