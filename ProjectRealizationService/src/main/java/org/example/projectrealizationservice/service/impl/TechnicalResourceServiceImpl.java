package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.TaskResourceAssignmentDTO;
import org.example.projectrealizationservice.dto.TechnicalResourceDTO;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TaskResourceAssignment;
import org.example.projectrealizationservice.model.TechnicalResource;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.repository.TaskResourceAssignmentRepository;
import org.example.projectrealizationservice.repository.TechnicalResourceRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.service.TechnicalResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnicalResourceServiceImpl implements TechnicalResourceService {

    private final TechnicalResourceRepository technicalResourceRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final TaskRepository taskRepository;

    @Override
    public void createTechnicalResource(TechnicalResourceCreationDTO technicalResource) {
        if (technicalResourceRepository.existsByName(technicalResource.getName())) {
            throw new RuntimeException("Technical resource with the same name already exists!");
        }

        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        technicalResourceRepository.save(TechnicalResource.builder()
                .name(technicalResource.getName())
                .description(technicalResource.getDescription())
                .quantity(technicalResource.getQuantity())
                .creatorId(creatorId)
                .build());
    }
    
    @Override
    public void assignTechnicalResourceToTask(Long technicalResourceId, Long taskId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
        TechnicalResource technicalResource = technicalResourceRepository.findById(technicalResourceId)
                .orElseThrow(() -> new RuntimeException("Technical resource with that id does not exist!"));

        boolean alreadyAssigned = taskResourceAssignmentRepository.findByTask_Id(taskId).stream()
                .anyMatch(assignment -> Objects.equals(
                        assignment.getTechnicalResource().getId(), technicalResourceId));
        if (alreadyAssigned) {
            throw new RuntimeException("This technical resource is already assigned to the task!");
        }

        if (technicalResource.getQuantity() < quantity) {
            throw new RuntimeException("Not enough quantity of resource is available!");
        }

        technicalResource.setQuantity(technicalResource.getQuantity() - quantity);

        TaskResourceAssignment assignment = TaskResourceAssignment.builder()
                .technicalResource(technicalResource)
                .task(task)
                .quantity(quantity)
                .build();

        taskResourceAssignmentRepository.save(assignment);
        technicalResourceRepository.save(technicalResource);
    }
    
    @Override
    public List<TaskResourceAssignmentDTO> getTechnicalResourcesByTaskId(Long taskId) {
        return taskResourceAssignmentRepository.findByTask_Id(taskId)
                .stream()
                .map(assignment -> TaskResourceAssignmentDTO.fromAssignment(
                        assignment, assignment.getTechnicalResource()))
                .toList();
    }

    @Override
    public List<TechnicalResourceDTO> getAllTechnicalResources() {
        return technicalResourceRepository.findAll()
                .stream()
                .map(TechnicalResourceDTO::toDto)
                .toList();
    }

    @Override
    public TechnicalResourceDTO getTechnicalResourceByName(String name) {
        return technicalResourceRepository.findByName(name)
                .map(TechnicalResourceDTO::toDto)
                .orElseThrow(() -> new RuntimeException("Technical resource with that name does not exist!"));
    }

    @Override
    public void updateTechnicalResource(Long technicalResourceId, TechnicalResourceCreationDTO technicalResource) {
        TechnicalResource existing = technicalResourceRepository.findById(technicalResourceId)
                .orElseThrow(() -> new RuntimeException("Technical resource with that id does not exist!"));

        TechnicalResource sameName = technicalResourceRepository.findByName(technicalResource.getName()).orElse(null);
        if (sameName != null && !sameName.getId().equals(existing.getId())) {
            throw new RuntimeException("Technical resource with the same name already exists!");
        }

        existing.setName(technicalResource.getName());
        existing.setDescription(technicalResource.getDescription());
        if (technicalResource.getQuantity() != null) {
            existing.setQuantity(technicalResource.getQuantity());
        }
        technicalResourceRepository.save(existing);
    }

    @Override
    public void deleteTechnicalResource(Long technicalResourceId) {
        TechnicalResource existing = technicalResourceRepository.findById(technicalResourceId)
                .orElseThrow(() -> new RuntimeException("Technical resource with that id does not exist!"));
        taskResourceAssignmentRepository.deleteAll(
                taskResourceAssignmentRepository.findByTechnicalResource_Id(technicalResourceId));
        technicalResourceRepository.delete(existing);
    }
}
