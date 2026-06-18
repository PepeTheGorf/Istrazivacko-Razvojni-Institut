package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProblemReportDTO;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;
import org.example.projectrealizationservice.model.ProblemReport;
import org.example.projectrealizationservice.model.ProblemStatus;
import org.example.projectrealizationservice.model.Role;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.repository.ProblemReportRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.security.ResourceAuthorization;
import org.example.projectrealizationservice.security.SecurityUtils;
import org.example.projectrealizationservice.service.ProblemReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemReportServiceImpl implements ProblemReportService {

    private final ProblemReportRepository problemReportRepository;
    private final TaskRepository taskRepository;

    @Override
    public void createProblemReport(Long taskId, ProblemReportCreationDTO problemReport) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with that id does not exist!"));
        if (problemReport.getProblemType() == null) {
            throw new RuntimeException("Problem type is required");
        }
        if (problemReport.getDescription() == null || problemReport.getDescription().isBlank()) {
            throw new RuntimeException("Description is required");
        }

        Long creatorId = ResourceAuthorization.requireCurrentUserId();

        problemReportRepository.save(ProblemReport.builder()
                .task(task)
                .creatorId(creatorId)
                .reviewedById(null)
                .description(problemReport.getDescription())
                .problemType(problemReport.getProblemType())
                .status(ProblemStatus.OPEN)
                .build());
    }

    @Override
    public void updateProblemReport(Long problemReportId, ProblemReportCreationDTO problemReport) {
        ProblemReport existing = findOrThrow(problemReportId);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isCreator = Objects.equals(existing.getCreatorId(), currentUserId);
        boolean isManager = SecurityUtils.getCurrentRole() == Role.MANAGER;

        if (!isCreator && !isManager) {
            throw new RuntimeException("You do not have permission to update this problem report.");
        }

        if (isManager && !isCreator) {
            if (problemReport.getStatus() != null) {
                existing.setStatus(problemReport.getStatus());
            }
            if (problemReport.getReviewedById() != null) {
                existing.setReviewedById(problemReport.getReviewedById());
            } else if (problemReport.getStatus() != null && currentUserId != null) {
                existing.setReviewedById(currentUserId);
            }
            problemReportRepository.save(existing);
            return;
        }

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
    public void deleteProblemReport(Long problemReportId) {
        ProblemReport existing = findOrThrow(problemReportId);
        ResourceAuthorization.assertCurrentUserIsOwner(existing.getCreatorId());
        problemReportRepository.delete(existing);
    }

    @Override
    public ProblemReportDTO getProblemReportById(Long problemReportId) {
        return ProblemReportDTO.toDTO(findOrThrow(problemReportId));
    }

    @Override
    public List<ProblemReportDTO> getAllProblemsByTask(Long taskId) {
        return problemReportRepository.findByTask_Id(taskId).stream()
                .map(ProblemReportDTO::toDTO)
                .toList();
    }

    @Override
    public List<ProblemReportDTO> getMyProblemReports() {
        Long creatorId = ResourceAuthorization.requireCurrentUserId();
        return problemReportRepository.findByCreatorIdWithTaskOrderByReportedAtDesc(creatorId).stream()
                .map(ProblemReportDTO::toDTO)
                .toList();
    }

    private ProblemReport findOrThrow(Long problemReportId) {
        return problemReportRepository.findById(problemReportId)
                .orElseThrow(() -> new RuntimeException("ProblemReport with that id does not exist!"));
    }
}
