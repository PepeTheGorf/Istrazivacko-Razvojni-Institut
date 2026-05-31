package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRequestDTO {

    @NotBlank(message = "Naziv is required")
    private String naziv;
}
