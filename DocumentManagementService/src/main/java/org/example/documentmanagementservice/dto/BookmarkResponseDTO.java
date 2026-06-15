package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.Bookmark;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponseDTO {

    private UUID id;
    private UUID korisnikId;
    private UUID dokumentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookmarkResponseDTO fromEntity(Bookmark bookmark) {
        return BookmarkResponseDTO.builder()
                .id(bookmark.getId())
                .korisnikId(bookmark.getKorisnikId())
                .dokumentId(bookmark.getDokumentId())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .build();
    }
}
