package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.ProjectDTO;

import java.util.List;

public interface ProjectService {
    void createProject(ProjectDTO project, Long creatorId);

    void deleteProject(String projectId, Long creatorId);

    void updateProject(String projectId, ProjectDTO project, Long creatorId);

    ProjectDTO getProjectByName(String name, Long creatorId);

    ProjectDTO getProjectById(String projectId, Long creatorId);

    List<ProjectDTO> findAll(Long creatorId);

    List<ProjectDTO> findAllForSelection();

}
