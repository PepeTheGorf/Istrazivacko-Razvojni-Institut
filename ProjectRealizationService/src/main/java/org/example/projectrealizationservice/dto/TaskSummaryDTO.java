package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDTO {
    private String id;
    private String name;
    private String description;
    private String phaseName;
    private OffsetDateTime endDate;
    private List<String> assigneeNames;
    
    private List<TaskSummaryDTO> subTasks;
}
