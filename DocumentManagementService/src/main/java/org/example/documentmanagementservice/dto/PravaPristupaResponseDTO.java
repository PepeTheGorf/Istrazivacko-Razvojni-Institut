package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.documentmanagementservice.model.NivoPrava;
import org.example.documentmanagementservice.model.PravaPristupa;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PravaPristupaResponseDTO {

    private UUID id;
    private UUID korisnikId;
    private UUID dokumentId;
    private UUID folderId;
    private UUID projekatId;
    private NivoPrava nivo;
    private UUID dodeljivaoId;
    private LocalDateTime datumDodele;

    public static PravaPristupaResponseDTO fromEntity(PravaPristupa pravaPristupa) {
        return PravaPristupaResponseDTO.builder()
                .id(pravaPristupa.getId())
                .korisnikId(pravaPristupa.getKorisnikId())
                .dokumentId(pravaPristupa.getDokumentId())
                .folderId(pravaPristupa.getFolderId())
                .projekatId(pravaPristupa.getProjekatId())
                .nivo(pravaPristupa.getNivo())
                .dodeljivaoId(pravaPristupa.getDodeljivaoId())
                .datumDodele(pravaPristupa.getDatumDodele())
                .build();
    }
}
