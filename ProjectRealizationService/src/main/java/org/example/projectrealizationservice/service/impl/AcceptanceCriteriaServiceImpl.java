package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.AcceptanceCriteriaDTO;
import org.example.projectrealizationservice.dto.creation.AcceptanceCriteriaCreationDTO;
import org.example.projectrealizationservice.model.AcceptanceCriteria;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.repository.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.service.AcceptanceCriteriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AcceptanceCriteriaServiceImpl implements AcceptanceCriteriaService {

    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskRepository taskRepository;

    @Override
    public void create(AcceptanceCriteriaCreationDTO dto) {
        if (dto.getTaskId() == null) {
            throw new RuntimeException("taskId is required");
        }
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));

        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        acceptanceCriteriaRepository.save(AcceptanceCriteria.builder()
                .task(task)
                .name(dto.getName())
                .description(dto.getDescription())
                .completed(false)
                .creatorId(creatorId)
                .build());
    }

    @Override
    public AcceptanceCriteriaDTO getById(Long id) {
        return AcceptanceCriteriaDTO.toDto(findOrThrow(id));
    }

    @Override
    public List<AcceptanceCriteriaDTO> getByTaskId(Long taskId) {
        return acceptanceCriteriaRepository.findByTask_Id(taskId).stream()
                .map(AcceptanceCriteriaDTO::toDto)
                .toList();
    }

    @Override
    public void update(Long id, AcceptanceCriteriaCreationDTO dto) {
        AcceptanceCriteria existing = findOrThrow(id);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        acceptanceCriteriaRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        AcceptanceCriteria existing = findOrThrow(id);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());
        acceptanceCriteriaRepository.delete(existing);
    }
    
    @Override
    public void solve(Long id) {
        AcceptanceCriteria existing = findOrThrow(id);
        existing.setCompleted(!existing.isCompleted());
        
        acceptanceCriteriaRepository.save(existing);
    }

    private AcceptanceCriteria findOrThrow(Long id) {
        return acceptanceCriteriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acceptance criteria with that id does not exist!"));
    }
}
