package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.TipPodatka;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipMetapodatkaRequestDTO {

    @NotBlank(message = "Naziv is required")
    private String naziv;

    @NotNull(message = "Tip podatka is required")
    private TipPodatka tipPodatka;

    private boolean jeObavezan = false;

    private UUID tipDokumentaId;
}
