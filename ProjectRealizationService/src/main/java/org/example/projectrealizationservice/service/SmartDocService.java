package org.example.projectrealizationservice.service;

import lombok.RequiredArgsConstructor;
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

        template.setSections(dto.getSections().stream()
                .map(s -> TemplateSection.builder()
                        .title(s.getTitle())
                        .systemPrompt(s.getSystemPrompt())
                        .sectionOrder(s.getOrder())
                        .template(template)
                        .build())
                .collect(Collectors.toList()));

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
}