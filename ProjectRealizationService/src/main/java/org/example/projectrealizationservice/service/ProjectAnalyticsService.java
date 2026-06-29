package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.analytics.AnalyticsFilter;
import org.example.projectrealizationservice.dto.analytics.ProjectWorkflowAnalysisDTO;
import org.example.projectrealizationservice.dto.analytics.TaskTeamMemberStatsDTO;

import java.util.List;

public interface ProjectAnalyticsService {
    ProjectWorkflowAnalysisDTO analyzeProjectWorkflow(Long projectId, AnalyticsFilter filter);
    
    List<TaskTeamMemberStatsDTO> analyzeTaskTeamMemberStats(Long taskId, AnalyticsFilter filter);

    List<TaskTeamMemberStatsDTO> analyzeProjectTeamMemberStats(Long projectId, AnalyticsFilter filter);
}
