package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTagRequestDTO {

    private String prompt;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Builder.Default
    private double similarityThreshold = 0.30;

    @NotEmpty
    private List<String> tagNames;

    @NotEmpty
    private List<UUID> documentIds;
}
