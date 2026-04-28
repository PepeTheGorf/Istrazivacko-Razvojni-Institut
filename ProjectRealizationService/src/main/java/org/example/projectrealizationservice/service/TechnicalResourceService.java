package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;

public interface TechnicalResourceService {
    void createTechnicalResource(TechnicalResourceCreationDTO technicalResource);

    TechnicalResourceCreationDTO getTechnicalResourceById(String technicalResourceId);

    void updateTechnicalResource(String technicalResourceId, TechnicalResourceCreationDTO technicalResource);

    void deleteTechnicalResource(String technicalResourceId);
}
