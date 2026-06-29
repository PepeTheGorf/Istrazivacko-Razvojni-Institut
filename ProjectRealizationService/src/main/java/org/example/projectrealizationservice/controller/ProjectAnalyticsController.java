package org.example.projectrealizationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.analytics.AnalyticsFilter;
import org.example.projectrealizationservice.dto.analytics.ProjectWorkflowAnalysisDTO;
import org.example.projectrealizationservice.dto.analytics.TaskTeamMemberStatsDTO;
import org.example.projectrealizationservice.service.ProjectAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class ProjectAnalyticsController {

    private final ProjectAnalyticsService projectAnalyticsService;

    @GetMapping("/projects/{projectId}/workflow")
    public ProjectWorkflowAnalysisDTO analyzeProjectWorkflow(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long taskId
    ) {
        return projectAnalyticsService.analyzeProjectWorkflow(
                projectId,
                new AnalyticsFilter(from, to, memberId, taskId)
        );
    }

    @GetMapping("/tasks/{taskId}/team-members")
    public List<TaskTeamMemberStatsDTO> analyzeTaskTeamMemberStats(
            @PathVariable Long taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Long memberId
    ) {
        return projectAnalyticsService.analyzeTaskTeamMemberStats(
                taskId,
                new AnalyticsFilter(from, to, memberId, taskId)
        );
    }

    @GetMapping("/projects/{projectId}/team-members")
    public List<TaskTeamMemberStatsDTO> analyzeProjectTeamMemberStats(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long taskId
    ) {
        return projectAnalyticsService.analyzeProjectTeamMemberStats(
                projectId,
                new AnalyticsFilter(from, to, memberId, taskId)
        );
    }
}
