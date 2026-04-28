package org.example.projectrealizationservice.dto.analytics;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcceptanceCriteriaCompletionSummary {
    private String taskName;
    private Long totalCriteria;
    private Long completedCriteria;
}
