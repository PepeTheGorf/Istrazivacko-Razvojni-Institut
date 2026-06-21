package org.example.projectrealizationservice.saga.smartdoc;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmartDocSagaContext {

    private final String researcherId;
    private final String sectionTitle;
    private final String generatedContent;
    private final String categoryName;
    private final String domainName;
    private final String templateName;

    private final Integer rating;
    private final String feedbackComment;
}
