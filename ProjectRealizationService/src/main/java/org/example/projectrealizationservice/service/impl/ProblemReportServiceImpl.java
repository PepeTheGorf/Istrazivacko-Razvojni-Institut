package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProblemReportDTO;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;
import org.example.projectrealizationservice.model.ProblemStatus;
import org.example.projectrealizationservice.model.sql.ProblemReport;
import org.example.projectrealizationservice.repository.neo4j.TaskRepository;
import org.example.projectrealizationservice.repository.sql.ProblemReportRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.service.ProblemReportService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemReportServiceImpl implements ProblemReportService {

    private final ProblemReportRepository problemReportRepository;
    private final TaskRepository taskRepository;

    @Override
    public void createProblemReport(String taskId, ProblemReportCreationDTO problemReport) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task with that id does not exist!");
        }
        if (problemReport.getProblemType() == null) {
            throw new RuntimeException("problemType is required");
        }
        if (problemReport.getDescription() == null || problemReport.getDescription().isBlank()) {
            throw new RuntimeException("description is required");
        }

        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        problemReportRepository.save(ProblemReport.builder()
                .taskId(taskId)
                .creatorId(creatorId)
                .reviewedById(problemReport.getReviewedById())
                .description(problemReport.getDescription())
                .problemType(problemReport.getProblemType())
                .status(problemReport.getStatus() != null ? problemReport.getStatus() : ProblemStatus.OPEN)
                .build());
    }

    @Override
    public void updateProblemReport(String problemReportId, ProblemReportCreationDTO problemReport) {
        ProblemReport existing = findOrThrow(problemReportId);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());

        if (problemReport.getReviewedById() != null) {
            existing.setReviewedById(problemReport.getReviewedById());
        }
        if (problemReport.getDescription() != null) {
            existing.setDescription(problemReport.getDescription());
        }
        if (problemReport.getProblemType() != null) {
            existing.setProblemType(problemReport.getProblemType());
        }
        if (problemReport.getStatus() != null) {
            existing.setStatus(problemReport.getStatus());
        }

        problemReportRepository.save(existing);
    }

    @Override
    public void deleteProblemReport(String problemReportId) {
        ProblemReport existing = findOrThrow(problemReportId);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());
        problemReportRepository.delete(existing);
    }

    @Override
    public ProblemReportDTO getProblemReportById(String problemReportId) {
        return ProblemReportDTO.toDTO(findOrThrow(problemReportId));
    }

    @Override
    public List<ProblemReportDTO> getAllProblemsByTask(String taskId) {
        return problemReportRepository.findByTaskId(taskId).stream()
                .map(ProblemReportDTO::toDTO)
                .toList();
    }

    private ProblemReport findOrThrow(String problemReportId) {
        return problemReportRepository.findById(Long.parseLong(problemReportId))
                .orElseThrow(() -> new RuntimeException("ProblemReport with that id does not exist!"));
    }
}
