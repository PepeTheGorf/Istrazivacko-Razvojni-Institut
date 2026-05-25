package org.example.projectrealizationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projectrealizationservice.model.Task;

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
    private Long creatorId;
    private Long assigneeId;

    private WorkflowDTO workflow;
    private List<TechnicalResourceDTO> technicalResources;
    private List<AcceptanceCriteriaDTO> acceptanceCriteria;
    private List<ProjectTaskDTO> subTasks;

    public static ProjectTaskDTO fromTask(Task task) {
        List<TechnicalResourceDTO> technicalResources = task.getTechnicalResources() == null
                ? List.of()
                : task.getTechnicalResources().stream()
                .map(ra -> TechnicalResourceDTO.toDto(ra.getTechnicalResource()))
                .toList();
        List<AcceptanceCriteriaDTO> acceptanceCriteria = task.getAcceptanceCriteria() == null
                ? List.of()
                : task.getAcceptanceCriteria().stream()
                .map(AcceptanceCriteriaDTO::toDto)
                .toList();

        return ProjectTaskDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .phaseName(task.getPhase() != null ? task.getPhase().getName() : null)
                .creatorId(task.getCreatorId())
                .assigneeId(task.getAssigneeId())
                .workflow(WorkflowDTO.toDTO(task.getWorkflow()))
                .technicalResources(technicalResources)
                .acceptanceCriteria(acceptanceCriteria)
                .build();
    }
}
