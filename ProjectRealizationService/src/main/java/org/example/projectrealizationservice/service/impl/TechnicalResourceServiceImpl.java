package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.TechnicalResourceDTO;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;
import org.example.projectrealizationservice.model.sql.TechnicalResource;
import org.example.projectrealizationservice.repository.sql.TechnicalResourceRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.service.TechnicalResourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicalResourceServiceImpl implements TechnicalResourceService {

    private final TechnicalResourceRepository technicalResourceRepository;

    @Override
    public void createTechnicalResource(TechnicalResourceCreationDTO technicalResource) {
        if (technicalResourceRepository.existsByName(technicalResource.getName())) {
            throw new RuntimeException("Technical resource with the same name already exists!");
        }

        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        technicalResourceRepository.save(TechnicalResource.builder()
                .name(technicalResource.getName())
                .description(technicalResource.getDescription())
                .creatorId(creatorId)
                .build());
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
    public void updateTechnicalResource(String technicalResourceId, TechnicalResourceCreationDTO technicalResource) {
        TechnicalResource existing = technicalResourceRepository.findById(Long.parseLong(technicalResourceId))
                .orElseThrow(() -> new RuntimeException("Technical resource with that id does not exist!"));

        TechnicalResource sameName = technicalResourceRepository.findByName(technicalResource.getName()).orElse(null);
        if (sameName != null && !sameName.getId().equals(existing.getId())) {
            throw new RuntimeException("Technical resource with the same name already exists!");
        }

        existing.setName(technicalResource.getName());
        existing.setDescription(technicalResource.getDescription());
        technicalResourceRepository.save(existing);
    }

    @Override
    public void deleteTechnicalResource(String technicalResourceId) {
        TechnicalResource existing = technicalResourceRepository.findById(Long.parseLong(technicalResourceId))
                .orElseThrow(() -> new RuntimeException("Technical resource with that id does not exist!"));
        technicalResourceRepository.delete(existing);
    }
}
