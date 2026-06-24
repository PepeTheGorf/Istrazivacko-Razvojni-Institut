package org.example.documentmanagementservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class DokumentSearchRequestDTO {

    private String naslov;
    private String autor;
    private UUID tipDokumentaId;
    private String tag;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String projektId;
    private List<MetadataFilterDTO> metadataFilters;

    @Data
    public static class MetadataFilterDTO {
        private UUID tipMetapodatkaId;
        private String vrednost;
    }
}
