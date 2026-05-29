package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.Metapodatak;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetapodatakResponseDTO {

    private UUID id;
    private UUID dokumentId;
    private UUID tipMetapodatkaId;
    private String vrednost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MetapodatakResponseDTO fromEntity(Metapodatak metapodatak) {
        return MetapodatakResponseDTO.builder()
                .id(metapodatak.getId())
                .dokumentId(metapodatak.getDokumentId())
                .tipMetapodatkaId(metapodatak.getTipMetapodatkaId())
                .vrednost(metapodatak.getVrednost())
                .createdAt(metapodatak.getCreatedAt())
                .updatedAt(metapodatak.getUpdatedAt())
                .build();
    }
}
