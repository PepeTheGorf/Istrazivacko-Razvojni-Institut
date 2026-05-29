package org.example.documentmanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DokumentRequestDTO {

    @NotBlank
    private String naslov;

    @NotBlank(message = "Author id is required")
    private String authorId;

    private String authorName;

    private String sadrzaj;

    private String projektId;

    private String projectName;

    private UUID tipDokumentaId;

    // list of tag names
    private List<String> tagovi;

    // metadata: tipMetapodatkaId -> vrednost
    private List<MetapodatakCreateDTO> metapodaci;
}
