package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.analytics.ProjectWorkflowAnalysisDTO;
import org.example.projectrealizationservice.dto.analytics.TaskTeamMemberStatsDTO;

import java.util.List;

public interface ProjectAnalyticsService {
    ProjectWorkflowAnalysisDTO analyzeProjectWorkflow(Long projectId);
    
    List<TaskTeamMemberStatsDTO> analyzeTaskTeamMemberStats(Long taskId);

    List<TaskTeamMemberStatsDTO> analyzeProjectTeamMemberStats(Long projectId);
}
