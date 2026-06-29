package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.TipMetapodatka;
import org.example.documentmanagementservice.model.TipPodatka;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipMetapodatkaResponseDTO {

    private UUID id;
    private String naziv;
    private TipPodatka tipPodatka;
    private boolean jeObavezan;
    private UUID tipDokumentaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TipMetapodatkaResponseDTO fromEntity(TipMetapodatka tipMetapodatka) {
        return TipMetapodatkaResponseDTO.builder()
                .id(tipMetapodatka.getId())
                .naziv(tipMetapodatka.getNaziv())
                .tipPodatka(tipMetapodatka.getTipPodatka())
                .jeObavezan(tipMetapodatka.isJeObavezan())
                .tipDokumentaId(tipMetapodatka.getTipDokumentaId())
                .createdAt(tipMetapodatka.getCreatedAt())
                .updatedAt(tipMetapodatka.getUpdatedAt())
                .build();
    }
}
