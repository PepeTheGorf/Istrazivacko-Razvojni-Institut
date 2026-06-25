package org.example.documentmanagementservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dokument_verzija")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DokumentVerzija {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID dokumentId;

    @Column(nullable = false)
    private Integer verzijaBroj;

    @Column(nullable = false)
    private String naslov;

    @Column(columnDefinition = "text")
    private String sadrzaj;

    private UUID sacuvaoId;

    @Column(nullable = false)
    private Instant datumKreiranja;
}
