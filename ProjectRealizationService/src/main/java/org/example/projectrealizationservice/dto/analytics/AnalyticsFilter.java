package org.example.projectrealizationservice.dto.analytics;

import java.time.OffsetDateTime;

public record AnalyticsFilter(
        OffsetDateTime from,
        OffsetDateTime to,
        Long memberId,
        Long taskId
) {
    public static AnalyticsFilter empty() {
        return new AnalyticsFilter(null, null, null, null);
    }
}
