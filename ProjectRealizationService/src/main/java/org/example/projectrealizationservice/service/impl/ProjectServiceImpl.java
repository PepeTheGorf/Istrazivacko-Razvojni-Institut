package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProjectDTO;
import org.example.projectrealizationservice.model.Project;
import org.example.projectrealizationservice.repository.ProjectRepository;
import org.example.projectrealizationservice.service.ProjectService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public void createProject(ProjectDTO project) {
        Project existingProject = projectRepository.findByName(project.getName())
                .orElse(null);
        if (existingProject != null) {
            throw new RuntimeException("Project with the same name already exists");
        }

        Project projectToSave = Project.builder()
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
        projectRepository.save(projectToSave);
    }

    @Override
    public void deleteProject(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElse(null);
        if (project == null) {
            throw new RuntimeException("Project with that id does not exist!");
        }
        projectRepository.delete(project);
    }

    @Override
    public void updateProject(String projectId, ProjectDTO project) {
        Project existingProject = projectRepository.findById(projectId)
                .orElse(null);
        if (existingProject == null) {
            throw new RuntimeException("Project with that id does not exist!");
        }
        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setStartDate(project.getStartDate());
        existingProject.setEndDate(project.getEndDate());
        projectRepository.save(existingProject);
    }

    @Override
    public ProjectDTO getProjectById(String projectId) {
        return ProjectDTO.toDTO(projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with that id does not exist!")));
    }

    @Override
    public List<ProjectDTO> findAll() {
        return projectRepository.findAll().stream()
                .map(ProjectDTO::toDTO)
                .toList();
    }

    @Override
    public ProjectDTO getProjectByName(String name) {
        return ProjectDTO.toDTO(projectRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Project with that name does not exist!")));
    }

    @Override
    public List<ProjectDTO> findProjectsByWorkflowWithMinTaskCount(String workflowName, long minTaskCount) {
        return projectRepository.findProjectsByWorkflowWithMinTaskCount(workflowName, minTaskCount)
                .stream()
                .map(ProjectDTO::toDTO)
                .toList();
    }

    @Override
    public List<ProjectDTO> findProjectsWithDelayedTasks(OffsetDateTime currentDate) {
        return projectRepository.findProjectsWithDelayedTasks(currentDate)
                .stream()
                .map(ProjectDTO::toDTO)
                .toList();
    }

    @Override
    public List<ProjectDTO> findProjectsWithHighTechnicalResourceWorkload(OffsetDateTime startDate, OffsetDateTime endDate) {
        return projectRepository.findProjectsWithHighTechnicalResourceWorkload(startDate, endDate)
                .stream()
                .map(ProjectDTO::toDTO)
                .toList();
    }
}
