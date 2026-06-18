package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.TechnicalResourceDTO;
import org.example.projectrealizationservice.dto.TaskResourceAssignmentDTO;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;

import java.util.List;

public interface TechnicalResourceService {
    void createTechnicalResource(TechnicalResourceCreationDTO technicalResource);

    List<TechnicalResourceDTO> getAllTechnicalResources();

    TechnicalResourceDTO getTechnicalResourceByName(String name);

    void updateTechnicalResource(Long technicalResourceId, TechnicalResourceCreationDTO technicalResource);

    void deleteTechnicalResource(Long technicalResourceId);
    
    void assignTechnicalResourceToTask(Long technicalResourceId, Long taskId, Integer quantity);
    
    List<TaskResourceAssignmentDTO> getTechnicalResourcesByTaskId(Long taskId);
}
