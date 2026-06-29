package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.DokumentTag;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DokumentTagResponseDTO {

    private UUID dokumentId;
    private UUID tagId;
    private LocalDateTime createdAt;

    public static DokumentTagResponseDTO fromEntity(DokumentTag dokumentTag) {
        return DokumentTagResponseDTO.builder()
                .dokumentId(dokumentTag.getDokumentId())
                .tagId(dokumentTag.getTagId())
                .createdAt(dokumentTag.getCreatedAt())
                .build();
    }
}
