package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectDTO;
import org.example.projectrealizationservice.model.sql.Project;
import org.example.projectrealizationservice.repository.sql.ProjectRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public void createProject(ProjectDTO project) {
        if (projectRepository.findByName(project.getName()).isPresent()) {
            throw new RuntimeException("Project with the same name already exists");
        }

        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        Project projectToSave = Project.builder()
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .creatorId(creatorId)
                .build();
        projectRepository.save(projectToSave);
    }

    @Override
    public void deleteProject(String projectId) {
        Project project = findAccessibleProjectOrThrow(projectId);
        ResourceAuthorization.assertCurrentUserIsOwner(project.getCreatorId());
        projectRepository.delete(project);
    }

    @Override
    public void updateProject(String projectId, ProjectDTO project) {
        Project existingProject = findAccessibleProjectOrThrow(projectId);
        ResourceAuthorization.assertCurrentUserIsOwner(existingProject.getCreatorId());

        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setStartDate(project.getStartDate());
        existingProject.setEndDate(project.getEndDate());
        projectRepository.save(existingProject);
    }

    @Override
    public List<ProjectDTO> findAll() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return projectRepository.findAll().stream()
                .filter(project -> Objects.equals(project.getCreatorId(), currentUserId))
                .map(ProjectDTO::toDTO)
                .toList();
    }

    @Override
    public ProjectDTO getProjectByName(String name) {
        Project project = projectRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Project with that name does not exist!"));
        assertAccessibleProject(project);
        return ProjectDTO.toDTO(project);
    }

    @Override
    public ProjectDTO getProjectById(String projectId) {
        return ProjectDTO.toDTO(findAccessibleProjectOrThrow(projectId));
    }

    private Project findAccessibleProjectOrThrow(String projectId) {
        Project project = projectRepository.findById(Long.parseLong(projectId))
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
        assertAccessibleProject(project);
        return project;
    }

    private void assertAccessibleProject(Project project) {
        if (!Objects.equals(project.getCreatorId(), SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("You do not have access to this project.");
        }
    }
}
