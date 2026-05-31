package org.example.documentmanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DokumentTagId implements Serializable {

    private UUID dokumentId;
    private UUID tagId;
}
