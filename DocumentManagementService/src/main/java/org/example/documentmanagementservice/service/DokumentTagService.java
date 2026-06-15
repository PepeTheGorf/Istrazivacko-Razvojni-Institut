package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.DokumentTagRequestDTO;
import org.example.documentmanagementservice.dto.DokumentTagResponseDTO;
import org.example.documentmanagementservice.dto.DokumentTagBulkRequestDTO;
import org.example.documentmanagementservice.exception.ResourceNotFoundException;
import org.example.documentmanagementservice.model.DokumentTag;
import org.example.documentmanagementservice.model.DokumentTagId;
import org.example.documentmanagementservice.repository.DokumentTagRepository;
import org.example.documentmanagementservice.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DokumentTagService {

    private final DokumentTagRepository repository;
    private final TagRepository tagRepository;

    public List<DokumentTagResponseDTO> findAll() {
        return repository.findAll().stream().map(DokumentTagResponseDTO::fromEntity).toList();
    }

    public DokumentTagResponseDTO findById(UUID dokumentId, UUID tagId) {
        DokumentTagId id = new DokumentTagId(dokumentId, tagId);
        return repository.findById(id)
                .map(DokumentTagResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("DokumentTag not found: " + id));
    }

    public DokumentTagResponseDTO create(DokumentTagRequestDTO request) {
        // ensure tag exists
        if (!tagRepository.existsById(request.getTagId())) {
            throw new ResourceNotFoundException("Tag not found: " + request.getTagId());
        }
        DokumentTagId id = new DokumentTagId(request.getDokumentId(), request.getTagId());
        if (repository.existsById(id)) {
            return DokumentTagResponseDTO.fromEntity(repository.findById(id).get());
        }
        DokumentTag entity = DokumentTag.builder()
                .dokumentId(request.getDokumentId())
                .tagId(request.getTagId())
                .build();
        return DokumentTagResponseDTO.fromEntity(repository.save(entity));
    }

    public void delete(UUID dokumentId, UUID tagId) {
        DokumentTagId id = new DokumentTagId(dokumentId, tagId);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("DokumentTag not found: " + id);
        }
        repository.deleteById(id);
    }

    public List<DokumentTagResponseDTO> findByDokumentId(UUID dokumentId) {
        return repository.findByDokumentId(dokumentId).stream().map(DokumentTagResponseDTO::fromEntity).toList();
    }

    public List<DokumentTagResponseDTO> findByTagId(UUID tagId) {
        return repository.findByTagId(tagId).stream().map(DokumentTagResponseDTO::fromEntity).toList();
    }

    public void deleteByDokumentId(UUID dokumentId) {
        repository.deleteByDokumentId(dokumentId);
    }

    public List<DokumentTagResponseDTO> bulkAssign(DokumentTagBulkRequestDTO request) {
        // ensure tags exist
        for (UUID tagId : request.getTagIds()) {
            if (!tagRepository.existsById(tagId)) {
                throw new ResourceNotFoundException("Tag not found: " + tagId);
            }
        }
        for (UUID tagId : request.getTagIds()) {
            DokumentTagId id = new DokumentTagId(request.getDokumentId(), tagId);
            if (!repository.existsById(id)) {
                DokumentTag entity = DokumentTag.builder()
                        .dokumentId(request.getDokumentId())
                        .tagId(tagId)
                        .build();
                repository.save(entity);
            }
        }
        return findByDokumentId(request.getDokumentId());
    }
}
