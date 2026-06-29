package org.example.projectrealizationservice.dto.smartdocs;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class PromptVersionDTO {
    private Long id;
    private String content;
    private Integer versionNumber;
    private boolean active;
    private OffsetDateTime createdAt;
    private Double averageRating;
    private Integer feedbackCount;
    private List<String> feedbackComments;
}