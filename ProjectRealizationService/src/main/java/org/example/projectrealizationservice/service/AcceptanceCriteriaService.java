package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.AcceptanceCriteriaDTO;
import org.example.projectrealizationservice.dto.creation.AcceptanceCriteriaCreationDTO;

import java.util.List;

public interface AcceptanceCriteriaService {
    void create(AcceptanceCriteriaCreationDTO dto);

    AcceptanceCriteriaDTO getById(String id);

    List<AcceptanceCriteriaDTO> getByTaskId(String taskId);

    void update(String id, AcceptanceCriteriaCreationDTO dto);

    void delete(String id);
}
