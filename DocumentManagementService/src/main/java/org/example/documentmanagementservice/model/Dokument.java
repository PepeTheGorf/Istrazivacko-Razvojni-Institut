package org.example.documentmanagementservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dokumenti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dokument {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String naslov;

    @Column(columnDefinition = "text")
    private String sadrzaj;

    @Column(nullable = false)
    private UUID authorId;

    private String authorName;

    private UUID projektId;

    private String projectName;

    private UUID tipDokumentaId;

    private Instant createdAt;

    private String vectorDocumentId;
}
