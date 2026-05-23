package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.ProblemReportDTO;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;

import java.util.List;

public interface ProblemReportService {
    void createProblemReport(ProblemReportCreationDTO problemReport);
    void updateProblemReport(String problemReportId, ProblemReportCreationDTO problemReport);
    void deleteProblemReport(String problemReportId);

    ProblemReportDTO getProblemReportById(String problemReportId);
    List<ProblemReportDTO> getAllProblemsByTask(String taskId);
}

