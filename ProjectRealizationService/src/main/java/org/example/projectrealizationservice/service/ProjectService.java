package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.ProjectDTO;

import java.util.List;

public interface ProjectService {
    void createProject(ProjectDTO project);

    void deleteProject(String projectId);

    void updateProject(String projectId, ProjectDTO project);

    ProjectDTO getProjectByName(String name);

    ProjectDTO getProjectById(String projectId);

    List<ProjectDTO> findAll();
}
