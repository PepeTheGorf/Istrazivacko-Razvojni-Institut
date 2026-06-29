package org.example.projectrealizationservice.dto.analytics;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskTeamMemberStatsDTO {
    private Long memberId;
    private String memberName;

    private int totalAssignedTasks;
    private int completedTasks;
    private int activeTasks;
    private int overdueTasks;
}
