package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;
import org.example.projectrealizationservice.model.TechnicalResource;
import org.example.projectrealizationservice.repository.TechnicalResourceRepository;
import org.example.projectrealizationservice.service.TechnicalResourceService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TechnicalResourceServiceImpl implements TechnicalResourceService {
    private final TechnicalResourceRepository technicalResourceRepository;


    @Override
    public void createTechnicalResource(TechnicalResourceCreationDTO technicalResource) {
        if(technicalResourceRepository.existsByName(technicalResource.getName())) {
            throw new RuntimeException("Technical resource with the same name already exists!");
        }
        TechnicalResource technicalResourceToSave = TechnicalResource.builder()
                .name(technicalResource.getName())
                .description(technicalResource.getDescription())
                .build();
        technicalResourceRepository.save(technicalResourceToSave);
    }

    @Override
    public TechnicalResourceCreationDTO getTechnicalResourceById(String technicalResourceId) {
        TechnicalResource technicalResource = technicalResourceRepository.findById(technicalResourceId)
                .orElseThrow(() -> new RuntimeException("Technical resource with that id does not exist!"));
        return TechnicalResourceCreationDTO.builder()
                .name(technicalResource.getName())
                .description(technicalResource.getDescription())
                .build();
    }

    @Override
    public void updateTechnicalResource(String technicalResourceId, TechnicalResourceCreationDTO technicalResource) {
        TechnicalResource existing = technicalResourceRepository.findById(technicalResourceId)
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
        TechnicalResource existing = technicalResourceRepository.findById(technicalResourceId)
                .orElseThrow(() -> new RuntimeException("Technical resource with that id does not exist!"));
        technicalResourceRepository.delete(existing);
    }
}
