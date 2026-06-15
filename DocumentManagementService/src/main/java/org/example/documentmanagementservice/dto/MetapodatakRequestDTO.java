package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetapodatakRequestDTO {

    @NotNull(message = "Dokument id is required")
    private UUID dokumentId;

    @NotNull(message = "Tip metapodatka id is required")
    private UUID tipMetapodatkaId;

    @NotBlank(message = "Vrednost is required")
    private String vrednost;
}
