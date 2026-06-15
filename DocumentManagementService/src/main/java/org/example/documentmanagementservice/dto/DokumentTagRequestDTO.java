package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DokumentTagRequestDTO {

    @NotNull(message = "Dokument id is required")
    private UUID dokumentId;

    @NotNull(message = "Tag id is required")
    private UUID tagId;
}
