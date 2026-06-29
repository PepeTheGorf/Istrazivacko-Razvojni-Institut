package org.example.projectrealizationservice.dto.analytics;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectWorkflowAnalysisDTO {
    private Long projectId;
    private String projectName;

    private List<PhaseAnalyticsDTO> phaseAnalytics;

    private int totalTasks;
    private int completedTasks;
    private int activeTasks;
    private int overdueTasks;

    private double totalTaskDurationSeconds;
    private double averageTaskDurationSeconds;

    private List<TaskPhaseHistoryEntryDTO> taskPhaseHistory;
}
