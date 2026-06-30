package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDTO {
    private Long id;
    private String name;
    private String description;
    private String phaseName;
    private Integer phaseOrder;
    private Long phaseId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private List<Long> assigneeIds;
    private List<String> assigneeNames;
    private boolean completed;
    private boolean overdue;
    private List<TaskSummaryDTO> subTasks;
}
