package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectDTO;
import org.example.projectrealizationservice.model.sql.Project;
import org.example.projectrealizationservice.repository.sql.ProjectRepository;
import org.example.projectrealizationservice.service.ProjectService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    @CacheEvict(value = "projects", key = "#creatorId")
    public void createProject(ProjectDTO project, Long creatorId) {
        if (projectRepository.findByName(project.getName()).isPresent()) {
            throw new RuntimeException("Project with the same name already exists");
        }

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
    @CacheEvict(value = "projects", key = "#creatorId")
    public void deleteProject(String projectId, Long creatorId) {
        Project project = findAccessibleProjectOrThrow(projectId, creatorId);
        projectRepository.delete(project);
    }

    @Override
    @CacheEvict(value = "projects", key = "#creatorId")
    public void updateProject(String projectId, ProjectDTO project, Long creatorId) {
        Project existingProject = findAccessibleProjectOrThrow(projectId, creatorId);

        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setStartDate(project.getStartDate());
        existingProject.setEndDate(project.getEndDate());
        projectRepository.save(existingProject);
    }

    @Override
    @Cacheable(value = "projects", key = "#creatorId", condition = "#creatorId != null")
    public List<ProjectDTO> findAll(Long creatorId) {
        return projectRepository.findAll().stream()
                .filter(project -> Objects.equals(project.getCreatorId(), creatorId))
                .map(ProjectDTO::toDTO)
                .toList();
    }

    @Override
    public ProjectDTO getProjectByName(String name, Long creatorId) {
        Project project = projectRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Project with that name does not exist!"));
        assertAccessibleProject(project, creatorId);
        return ProjectDTO.toDTO(project);
    }

    @Override
    public ProjectDTO getProjectById(String projectId, Long creatorId) {
        return ProjectDTO.toDTO(findAccessibleProjectOrThrow(projectId, creatorId));
    }

    private Project findAccessibleProjectOrThrow(String projectId, Long creatorId) {
        Project project = projectRepository.findById(Long.parseLong(projectId))
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!"));
        assertAccessibleProject(project, creatorId);
        return project;
    }

    private void assertAccessibleProject(Project project, Long creatorId) {
        if (!Objects.equals(project.getCreatorId(), creatorId)) {
            throw new RuntimeException("You do not have access to this project.");
        }
    }
}
