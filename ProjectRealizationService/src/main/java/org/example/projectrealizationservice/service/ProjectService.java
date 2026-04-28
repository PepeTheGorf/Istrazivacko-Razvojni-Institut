package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.ProjectDTO;

import java.time.OffsetDateTime;
import java.util.List;

public interface ProjectService {
    void createProject(ProjectDTO project);

    void deleteProject(String projectId);

    void updateProject(String projectId, ProjectDTO project);

    ProjectDTO getProjectById(String projectId);

    ProjectDTO getProjectByName(String name);

    List<ProjectDTO> findAll();

    List<ProjectDTO> findProjectsByWorkflowWithMinTaskCount(String workflowName, long minTaskCount);

    List<ProjectDTO> findProjectsWithDelayedTasks(OffsetDateTime currentDate);

    List<ProjectDTO> findProjectsWithHighTechnicalResourceWorkload(OffsetDateTime startDate, OffsetDateTime endDate);

}
