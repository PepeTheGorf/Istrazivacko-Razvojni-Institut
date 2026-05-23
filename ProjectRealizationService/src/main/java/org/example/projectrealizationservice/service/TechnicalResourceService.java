package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.TechnicalResourceDTO;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;

import java.util.List;

public interface TechnicalResourceService {
    void createTechnicalResource(TechnicalResourceCreationDTO technicalResource);
    List<TechnicalResourceDTO> getAllTechnicalResources();
    TechnicalResourceDTO getTechnicalResourceByName(String name);
    void updateTechnicalResource(String technicalResourceId, TechnicalResourceCreationDTO technicalResource);
    void deleteTechnicalResource(String technicalResourceId);
}
