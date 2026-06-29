package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.TipDokumenta;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipDokumentaResponseDTO {

    private UUID id;
    private String naziv;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TipDokumentaResponseDTO fromEntity(TipDokumenta tipDokumenta) {
        return TipDokumentaResponseDTO.builder()
                .id(tipDokumenta.getId())
                .naziv(tipDokumenta.getNaziv())
                .createdAt(tipDokumenta.getCreatedAt())
                .updatedAt(tipDokumenta.getUpdatedAt())
                .build();
    }
}
