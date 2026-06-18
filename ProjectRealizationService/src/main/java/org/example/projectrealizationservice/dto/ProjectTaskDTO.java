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
public class ProjectTaskDTO {
    private Long id;
    private String name;
    private String description;
    private String phaseName;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Long creatorId;
    private Long assigneeId;
    private List<Long> assigneeIds;

    private WorkflowDTO workflow;
    private List<TaskResourceAssignmentDTO> technicalResources;
    private List<AcceptanceCriteriaDTO> acceptanceCriteria;
    private List<ProjectTaskDTO> subTasks;
}
