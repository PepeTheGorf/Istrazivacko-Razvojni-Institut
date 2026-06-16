package org.example.projectrealizationservice.service;

import lombok.RequiredArgsConstructor;

import org.example.projectrealizationservice.dto.smartdocs.DocumentResponseDTO;
import org.example.projectrealizationservice.dto.smartdocs.SmartDocumentSummaryDTO;
import org.example.projectrealizationservice.dto.smartdocs.TemplateCreationDTO;
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
    private final AiService aiService;

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
                        .systemPrompt(s.getSystemPrompt())
                        .sectionOrder(s.getOrder())
                        .template(template) 
                        .build();
                return ts;
            })
            .collect(Collectors.toList());

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
            .orElseThrow(() -> new RuntimeException("Dokument sa ID " + id + " zaista ne postoji u bazi"));

    return DocumentResponseDTO.builder()
            .id(doc.getId())
            .status(doc.getStatus())
            .templateName(doc.getTemplate() != null ? doc.getTemplate().getName() : "Nepoznat šablon")
            .sections(doc.getSections().stream()
                    .map(s -> DocumentResponseDTO.SectionResponseDTO.builder()
                            .id(s.getId())
                            .title(s.getTemplateSection() != null ? s.getTemplateSection().getTitle() : "Sekcija bez naslova")
                            .userInput(s.getUserInput())
                            .llmResult(s.getLlmResult())
                            .build())
                    .collect(Collectors.toList()))
            .build();
        }

       public List<SmartTemplate> getAllTemplates() {
          return templateRepository.findAll();
        }

        @Transactional("transactionManager")
        public void updateSectionInput(Long sectionId, String text) {
           DocumentSection section = documentSectionRepository.findById(sectionId)
            .orElseThrow(() -> new RuntimeException("Sekcija nije pronađena"));
            section.setUserInput(text);
        documentSectionRepository.save(section);
        }

        @Transactional(value = "transactionManager", readOnly = true)
        public List<SmartDocumentSummaryDTO> getMyDocuments(Long researcherId) {
            return documentRepository.findByResearcherId(researcherId).stream()
            .map(doc -> SmartDocumentSummaryDTO.builder()
                    .id(doc.getId())
                    .templateName(doc.getTemplate() != null ? doc.getTemplate().getName() : "Nepoznat šablon")
                    .status(doc.getStatus())
                    .createdAt(doc.getCreatedAt())
                    .build())
            .collect(Collectors.toList());
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
                contextBuilder.append("Sekcija [")
                        .append(s.getTemplateSection().getTitle())
                        .append("]: ")
                        .append(content)
                        .append("\n\n");
            }
        }
    }

    String context = contextBuilder.toString();

    String generatedResult = aiService.generateText(
            ts.getSystemPrompt(), 
            context, 
            currentSection.getUserInput()
    );

    currentSection.setLlmResult(generatedResult);
    documentSectionRepository.save(currentSection);

    return generatedResult;
}
}