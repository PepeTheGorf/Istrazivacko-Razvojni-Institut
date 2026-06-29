package org.example.projectrealizationservice.dto.analytics;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhaseAnalyticsDTO {
    private Long phaseId;
    private String phaseName;
    private int phaseOrder;

    private int currentTaskCount;

    private double averageSecondsInPhase;
}
