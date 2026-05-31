package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.TagRequestDTO;
import org.example.documentmanagementservice.dto.TagResponseDTO;
import org.example.documentmanagementservice.exception.ResourceNotFoundException;
import org.example.documentmanagementservice.model.Tag;
import org.example.documentmanagementservice.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository repository;

    public List<TagResponseDTO> findAll() {
        return repository.findAll().stream().map(TagResponseDTO::fromEntity).toList();
    }

    public TagResponseDTO findById(UUID id) {
        return TagResponseDTO.fromEntity(findEntityById(id));
    }

    public TagResponseDTO create(TagRequestDTO request) {
        Tag entity = Tag.builder()
                .naziv(request.getNaziv().trim())
                .build();
        return TagResponseDTO.fromEntity(repository.save(entity));
    }

    public TagResponseDTO update(UUID id, TagRequestDTO request) {
        Tag entity = findEntityById(id);
        entity.setNaziv(request.getNaziv().trim());
        return TagResponseDTO.fromEntity(repository.save(entity));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found: " + id);
        }
        repository.deleteById(id);
    }

    private Tag findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + id));
    }
}
