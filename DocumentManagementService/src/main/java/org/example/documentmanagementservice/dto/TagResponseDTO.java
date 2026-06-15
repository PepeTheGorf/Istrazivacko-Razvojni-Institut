package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponseDTO {

    private UUID id;
    private String naziv;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TagResponseDTO fromEntity(Tag tag) {
        return TagResponseDTO.builder()
                .id(tag.getId())
                .naziv(tag.getNaziv())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}
