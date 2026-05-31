package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequestDTO {

    @NotNull(message = "Korisnik id is required")
    private UUID korisnikId;

    @NotNull(message = "Dokument id is required")
    private UUID dokumentId;
}
