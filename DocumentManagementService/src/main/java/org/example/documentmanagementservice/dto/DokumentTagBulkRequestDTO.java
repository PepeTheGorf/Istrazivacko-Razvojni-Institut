package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DokumentTagBulkRequestDTO {

    @NotNull(message = "Dokument id is required")
    private UUID dokumentId;

    @NotNull(message = "Tag ids are required")
    private List<UUID> tagIds;
}
