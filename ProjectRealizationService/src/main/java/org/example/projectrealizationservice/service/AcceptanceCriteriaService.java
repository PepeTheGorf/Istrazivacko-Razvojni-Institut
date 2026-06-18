package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.AcceptanceCriteriaDTO;
import org.example.projectrealizationservice.dto.creation.AcceptanceCriteriaCreationDTO;

import java.util.List;

public interface AcceptanceCriteriaService {
    void create(AcceptanceCriteriaCreationDTO dto);

    AcceptanceCriteriaDTO getById(Long id);

    List<AcceptanceCriteriaDTO> getByTaskId(Long taskId);

    void update(Long id, AcceptanceCriteriaCreationDTO dto);

    void delete(Long id);
    
    void solve(Long id);
}
