package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.AcceptanceCriteriaDTO;
import org.example.projectrealizationservice.dto.creation.AcceptanceCriteriaCreationDTO;
import org.example.projectrealizationservice.model.sql.AcceptanceCriteria;
import org.example.projectrealizationservice.repository.neo4j.TaskRepository;
import org.example.projectrealizationservice.repository.sql.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.service.AcceptanceCriteriaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcceptanceCriteriaServiceImpl implements AcceptanceCriteriaService {

    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskRepository taskRepository;

    @Override
    public void create(AcceptanceCriteriaCreationDTO dto) {
        if (dto.getTaskId() == null || dto.getTaskId().isBlank()) {
            throw new RuntimeException("taskId is required");
        }
        if (!taskRepository.existsById(dto.getTaskId())) {
            throw new RuntimeException("Task with that id does not exist!");
        }

        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        acceptanceCriteriaRepository.save(AcceptanceCriteria.builder()
                .taskId(dto.getTaskId())
                .name(dto.getName())
                .description(dto.getDescription())
                .completed(false)
                .creatorId(creatorId)
                .build());
    }

    @Override
    public AcceptanceCriteriaDTO getById(String id) {
        return AcceptanceCriteriaDTO.toDto(findOrThrow(id));
    }

    @Override
    public List<AcceptanceCriteriaDTO> getByTaskId(String taskId) {
        return acceptanceCriteriaRepository.findByTaskId(taskId).stream()
                .map(AcceptanceCriteriaDTO::toDto)
                .toList();
    }

    @Override
    public void update(String id, AcceptanceCriteriaCreationDTO dto) {
        AcceptanceCriteria existing = findOrThrow(id);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        acceptanceCriteriaRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        AcceptanceCriteria existing = findOrThrow(id);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());
        acceptanceCriteriaRepository.delete(existing);
    }

    private AcceptanceCriteria findOrThrow(String id) {
        return acceptanceCriteriaRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Acceptance criteria with that id does not exist!"));
    }
}
