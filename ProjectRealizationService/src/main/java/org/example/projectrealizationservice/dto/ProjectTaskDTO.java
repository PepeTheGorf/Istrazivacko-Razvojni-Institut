package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskDTO {
    private String id;
    private String name;
    private String description;
    private String phaseName;

    private List<String> technicalResourceNames;
    private List<String> acceptanceCriteriaNames;

    private List<ProjectTaskDTO> subTasks;
}
