package org.example.projectrealizationservice.service;

import org.example.projectrealizationservice.dto.ProblemReportDTO;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;

import java.util.List;

public interface ProblemReportService {
    void createProblemReport(Long taskId, ProblemReportCreationDTO problemReport);

    void updateProblemReport(Long problemReportId, ProblemReportCreationDTO problemReport);

    void deleteProblemReport(Long problemReportId);

    ProblemReportDTO getProblemReportById(Long problemReportId);

    List<ProblemReportDTO> getAllProblemsByTask(Long taskId);

    List<ProblemReportDTO> getMyProblemReports();
}
