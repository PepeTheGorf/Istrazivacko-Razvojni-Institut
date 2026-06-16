package org.example.projectrealizationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.projectrealizationservice.dto.smartdocs.*;
import org.example.projectrealizationservice.model.sql.smartdocs.*;
import org.example.projectrealizationservice.repository.sql.smartdocs.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartDocService {

    private final SmartTemplateRepository templateRepository;
    private final DocumentDomainRepository domainRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final GeneratedDocumentRepository documentRepository;
    private final DocumentSectionRepository documentSectionRepository;
    private final SectionFeedbackRepository feedbackRepository;
    private final AiService aiService;
    private final PromptVersionRepository promptVersionRepository;
    private final TemplateSectionRepository templateSectionRepository;

    @Transactional("transactionManager")
    public void createTemplate(TemplateCreationDTO dto, Long creatorId) {
        DocumentDomain domain = dto.getDomainId() != null 
            ? domainRepository.findById(dto.getDomainId()).orElseThrow()
            : domainRepository.findByName(dto.getNewDomain())
                .orElseGet(() -> domainRepository.save(DocumentDomain.builder().name(dto.getNewDomain()).build()));

        DocumentCategory category = dto.getCategoryId() != null
            ? categoryRepository.findById(dto.getCategoryId()).orElseThrow()
            : categoryRepository.findByName(dto.getNewCategory())
                .orElseGet(() -> categoryRepository.save(DocumentCategory.builder().name(dto.getNewCategory()).build()));

        SmartTemplate template = SmartTemplate.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .domain(domain)
                .category(category)
                .creatorId(creatorId)
                .createdAt(OffsetDateTime.now())
                .build();

        List<TemplateSection> sections = dto.getSections().stream()
            .map(s -> {
        TemplateSection ts = TemplateSection.builder()
                .title(s.getTitle())
                .sectionOrder(s.getOrder())
                .template(template) 
                .build();
    
        PromptVersion v1 = PromptVersion.builder()
                .content(s.getSystemPrompt())
                .versionNumber(1)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .templateSection(ts)
                .build();
        
        ts.setPromptVersions(List.of(v1));
        return ts;
    }).collect(Collectors.toList());

        template.setSections(sections);
        templateRepository.save(template);
    }

    public List<SmartTemplate> getTemplatesByFilter(Long domainId, Long categoryId) {
        return templateRepository.findByDomainIdAndCategoryId(domainId, categoryId);
    }

    @Transactional("transactionManager")
    public GeneratedDocument createDocumentFromTemplate(Long templateId, Long researcherId) {
        SmartTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Šablon nije pronađen"));

        GeneratedDocument doc = GeneratedDocument.builder()
                .template(template)
                .researcherId(researcherId)
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();

        GeneratedDocument savedDoc = documentRepository.save(doc);

        List<DocumentSection> docSections = template.getSections().stream()
                .map(ts -> DocumentSection.builder()
                        .document(savedDoc)
                        .templateSection(ts)
                        .userInput("") 
                        .build())
                .collect(Collectors.toList());

        documentSectionRepository.saveAll(docSections);
        return savedDoc;
    }

    public List<DocumentDomain> getAllDomains() { return domainRepository.findAll(); }
    public List<DocumentCategory> getAllCategories() { return categoryRepository.findAll(); }

    @Transactional(value = "transactionManager", readOnly = true)
    public DocumentResponseDTO getDocumentById(Long id) {
        GeneratedDocument doc = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dokument nije pronađen"));

        return DocumentResponseDTO.builder()
            .id(doc.getId())
            .status(doc.getStatus())
            .templateName(doc.getTemplate() != null ? doc.getTemplate().getName() : "Nepoznat šablon")
            .sections(doc.getSections().stream()
                    .map(s -> DocumentResponseDTO.SectionResponseDTO.builder()
                            .id(s.getId())
                            .title(s.getTemplateSection() != null ? s.getTemplateSection().getTitle() : "Bez naslova")
                            .userInput(s.getUserInput())
                            .llmResult(s.getLlmResult())
                            .rating(s.getFeedback() != null ? s.getFeedback().getRating() : null)
                            .feedbackComment(s.getFeedback() != null ? s.getFeedback().getComment() : null)
                            .build())
                    .collect(Collectors.toList()))
            .build();
    }

    @Transactional(value = "transactionManager", readOnly = true)
    public List<SmartTemplateDTO> getAllTemplates() {
        return templateRepository.findAll().stream()
            .map(t -> SmartTemplateDTO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .description(t.getDescription())
                        .domain(t.getDomain())
                        .category(t.getCategory())
                        .sections(t.getSections())
                        .createdAt(t.getCreatedAt())
                        .averageRating(getAverageRatingForTemplate(t.getId())) 
                        .build())
            .collect(Collectors.toList());
    }

    @Transactional(value = "transactionManager", readOnly = true)
    public List<SmartDocumentSummaryDTO> getMyDocuments(Long researcherId) {
        return documentRepository.findByResearcherId(researcherId).stream()
            .map(doc -> {
                long total = doc.getSections().size();
                long generated = doc.getSections().stream()
                        .filter(s -> s.getLlmResult() != null && !s.getLlmResult().isBlank())
                        .count();
                int prog = total > 0 ? (int) ((generated * 100) / total) : 0;

                return SmartDocumentSummaryDTO.builder()
                        .id(doc.getId())
                        .templateName(doc.getTemplate() != null ? doc.getTemplate().getName() : "Nepoznat šablon")
                        .status(doc.getStatus())
                        .createdAt(doc.getCreatedAt())
                        .progress(prog)
                        .build();
            })
            .collect(Collectors.toList());
    }

    @Transactional("transactionManager")
    public void updateSectionInput(Long sectionId, String text) {
        DocumentSection section = documentSectionRepository.findById(sectionId)
            .orElseThrow(() -> new RuntimeException("Sekcija nije pronađena"));
        section.setUserInput(text);
        documentSectionRepository.save(section);
    }

    @Transactional("transactionManager")
    public String generateSectionContent(Long sectionId) {
        DocumentSection currentSection = documentSectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Sekcija nije pronađena"));

        GeneratedDocument doc = currentSection.getDocument();
        TemplateSection ts = currentSection.getTemplateSection();

        List<DocumentSection> allSections = doc.getSections();
        allSections.sort((a, b) -> a.getTemplateSection().getSectionOrder()
                .compareTo(b.getTemplateSection().getSectionOrder()));

        StringBuilder contextBuilder = new StringBuilder();
        for (DocumentSection s : allSections) {
            if (s.getTemplateSection().getSectionOrder() < ts.getSectionOrder()) {
                String content = s.getLlmResult();
                if (content != null && !content.isBlank()) {
                    contextBuilder.append("Sekcija [").append(s.getTemplateSection().getTitle()).append("]: ")
                            .append(content).append("\n\n");
                }
            }
        }

        String generatedResult = aiService.generateText(
        ts.getActivePromptContent(), 
        contextBuilder.toString(), 
        currentSection.getUserInput()
);
        currentSection.setLlmResult(generatedResult);
        documentSectionRepository.save(currentSection);
        return generatedResult;
    }

    @Transactional("transactionManager")
    public void completeDocument(Long docId) {
        GeneratedDocument doc = documentRepository.findById(docId).orElseThrow();
        doc.setStatus("COMPLETED");
        documentRepository.save(doc);
    }

    @Transactional("transactionManager")
    public void saveFeedback(Long sectionId, Integer rating, String comment) {
        DocumentSection section = documentSectionRepository.findById(sectionId).orElseThrow();
        SectionFeedback feedback = section.getFeedback();
        if (feedback == null) {
            feedback = SectionFeedback.builder().section(section).build();
        }
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedbackRepository.save(feedback);
    }

    @Transactional(value = "transactionManager", readOnly = true)
    public Double getAverageRatingForTemplate(Long templateId) {
        List<GeneratedDocument> docs = documentRepository.findAll().stream()
                .filter(d -> d.getTemplate() != null && d.getTemplate().getId().equals(templateId))
                .toList();

        return docs.stream()
                .flatMap(d -> d.getSections().stream())
                .filter(s -> s.getFeedback() != null)
                .mapToInt(s -> s.getFeedback().getRating())
                .average()
                .orElse(0.0);
    }

    @Transactional("transactionManager")
    public void deleteDocument(Long docId) {
    if (!documentRepository.existsById(docId)) {
        throw new RuntimeException("Dokument ne postoji.");
    }
    documentRepository.deleteById(docId);
    }

    @Transactional("transactionManager")
    public void createNewPromptVersion(Long sectionId, String newContent) {
    TemplateSection ts = templateSectionRepository.findById(sectionId).orElseThrow();
    
    ts.getPromptVersions().forEach(v -> v.setActive(false));

    int nextVersion = ts.getPromptVersions().stream()
            .mapToInt(PromptVersion::getVersionNumber)
            .max().orElse(0) + 1;

    PromptVersion newVersion = PromptVersion.builder()
            .content(newContent)
            .versionNumber(nextVersion)
            .active(true)
            .createdAt(OffsetDateTime.now())
            .templateSection(ts)
            .build();

    promptVersionRepository.save(newVersion);
}

    @Transactional("transactionManager")
    public void activateOldVersion(Long sectionId, Long versionId) {
    TemplateSection ts = templateSectionRepository.findById(sectionId).orElseThrow();
    
    ts.getPromptVersions().forEach(v -> {
        v.setActive(v.getId().equals(versionId));
    });
    
    templateSectionRepository.save(ts);
  }

  @Transactional(value = "transactionManager", readOnly = true)
  public List<PromptVersionDTO> getPromptHistory(Long sectionId) {
    return promptVersionRepository.findByTemplateSectionIdOrderByVersionNumberDesc(sectionId).stream()
            .map(v -> PromptVersionDTO.builder()
                    .id(v.getId())
                    .content(v.getContent())
                    .versionNumber(v.getVersionNumber())
                    .active(v.isActive())
                    .createdAt(v.getCreatedAt())
                    .build())
            .collect(Collectors.toList());
 }

 @Transactional(value = "transactionManager", readOnly = true)
public SmartTemplateDTO getTemplateById(Long id) {
    SmartTemplate t = templateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Šablon nije pronađen"));

    return SmartTemplateDTO.builder()
            .id(t.getId())
            .name(t.getName())
            .description(t.getDescription())
            .domain(t.getDomain())
            .category(t.getCategory())
            .sections(t.getSections()) 
            .createdAt(t.getCreatedAt())
            .averageRating(getAverageRatingForTemplate(t.getId()))
            .build();
}
}