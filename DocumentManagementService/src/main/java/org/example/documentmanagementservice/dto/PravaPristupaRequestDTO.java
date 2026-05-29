package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.NivoPrava;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PravaPristupaRequestDTO {

    @NotNull(message = "Korisnik id is required")
    private UUID korisnikId;

    private UUID dokumentId;

    private UUID folderId;

    private UUID projekatId;

    @NotNull(message = "Nivo is required")
    private NivoPrava nivo;

    @NotNull(message = "Dodeljivao id is required")
    private UUID dodeljivaoId;
}
