package org.example.projectrealizationservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.ProblemReportDTO;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;
import org.example.projectrealizationservice.model.ProblemReport;
import org.example.projectrealizationservice.repository.ProblemReportRepository;
import org.example.projectrealizationservice.service.ProblemReportService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemReportServiceImpl implements ProblemReportService {

    private final ProblemReportRepository problemReportRepository;

    @Override
    public void createProblemReport(ProblemReportCreationDTO problemReport) {
        ProblemReport toSave = ProblemReport.builder()
                .creatorId(problemReport.getCreatorId())
                .reviewedById(problemReport.getReviewedById())
                .description(problemReport.getDescription())
                .problemType(problemReport.getProblemType())
                .status(problemReport.getStatus())
                .build();
        problemReportRepository.save(toSave);
    }

    @Override
    public void updateProblemReport(String problemReportId, ProblemReportCreationDTO problemReport) {
        ProblemReport existing = problemReportRepository.findById(problemReportId)
                .orElseThrow(() -> new RuntimeException("ProblemReport with that id does not exist!"));

        if (problemReport.getCreatorId() != null) {
            existing.setCreatorId(problemReport.getCreatorId());
        }
        if (problemReport.getReviewedById() != null) {
            existing.setReviewedById(problemReport.getReviewedById());
        }
        existing.setDescription(problemReport.getDescription());
        existing.setProblemType(problemReport.getProblemType());
        if (problemReport.getStatus() != null) {
            existing.setStatus(problemReport.getStatus());
        }

        problemReportRepository.save(existing);
    }

    @Override
    public void deleteProblemReport(String problemReportId) {
        ProblemReport existing = problemReportRepository.findById(problemReportId)
                .orElseThrow(() -> new RuntimeException("ProblemReport with that id does not exist!"));
        problemReportRepository.delete(existing);
    }

    @Override
    public ProblemReportDTO getProblemReportById(String problemReportId) {
        return ProblemReportDTO.toDTO(problemReportRepository.findById(problemReportId)
                .orElseThrow(() -> new RuntimeException("ProblemReport with that id does not exist!")));
    }

    @Override
    public List<ProblemReportDTO> getAllProblemsByTask(String taskId) {
        return problemReportRepository.findAllByTaskId(taskId).stream()
                .map(ProblemReportDTO::toDTO)
                .toList();
    }
}

