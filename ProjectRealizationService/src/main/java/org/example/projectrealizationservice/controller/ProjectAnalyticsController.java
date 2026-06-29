package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.analytics.ProjectWorkflowAnalysisDTO;
import org.example.projectrealizationservice.dto.analytics.TaskTeamMemberStatsDTO;
import org.example.projectrealizationservice.service.ProjectAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class ProjectAnalyticsController {

    private final ProjectAnalyticsService projectAnalyticsService;

    @GetMapping("/projects/{projectId}/workflow")
    public ProjectWorkflowAnalysisDTO analyzeProjectWorkflow(@PathVariable Long projectId) {
        return projectAnalyticsService.analyzeProjectWorkflow(projectId);
    }

    @GetMapping("/tasks/{taskId}/team-members")
    public List<TaskTeamMemberStatsDTO> analyzeTaskTeamMemberStats(@PathVariable Long taskId) {
        return projectAnalyticsService.analyzeTaskTeamMemberStats(taskId);
    }

    @GetMapping("/projects/{projectId}/team-members")
    public List<TaskTeamMemberStatsDTO> analyzeProjectTeamMemberStats(@PathVariable Long projectId) {
        return projectAnalyticsService.analyzeProjectTeamMemberStats(projectId);
    }
}
