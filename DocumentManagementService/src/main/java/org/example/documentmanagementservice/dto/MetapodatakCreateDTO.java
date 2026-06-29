package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetapodatakCreateDTO {
    private UUID tipMetapodatkaId;
    private String vrednost;
}
