package org.example.projectrealizationanalyticsservice.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TimeRange {
    private OffsetDateTime from;
    private OffsetDateTime to;
}
