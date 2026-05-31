package org.example.projectrealizationanalyticsservice.analytics.model;

import java.time.OffsetDateTime;

public enum RangeType {

    LAST_HOUR,
    LAST_DAY,
    LAST_WEEK,
    LAST_MONTH,
    LAST_YEAR,;

    public TimeRange toTimeRange(OffsetDateTime now) {
        return switch (this) {
            case LAST_HOUR -> new TimeRange(now.minusHours(1), now);
            case LAST_DAY -> new TimeRange(now.minusDays(1), now);
            case LAST_WEEK -> new TimeRange(now.minusDays(7), now);
            case LAST_MONTH -> new TimeRange(now.minusMonths(1), now);
            case LAST_YEAR -> new TimeRange(now.minusYears(1),now);
        };
    }
}
