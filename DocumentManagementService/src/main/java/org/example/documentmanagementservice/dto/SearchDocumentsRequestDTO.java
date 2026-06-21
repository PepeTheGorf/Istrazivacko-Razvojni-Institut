package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocumentsRequestDTO {

    @NotBlank
    private String prompt;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Builder.Default
    private double similarityThreshold = 0.30;
}
