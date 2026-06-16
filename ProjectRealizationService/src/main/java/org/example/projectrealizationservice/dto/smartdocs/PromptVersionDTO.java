package org.example.projectrealizationservice.dto.smartdocs;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class PromptVersionDTO {
    private Long id;
    private String content;
    private Integer versionNumber;
    private boolean active;
    private OffsetDateTime createdAt;
}