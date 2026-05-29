package org.example.documentmanagementservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prava_pristupa")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PravaPristupa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID korisnikId;

    @Column(nullable = true)
    private UUID dokumentId;

    @Column(nullable = true)
    private UUID folderId;

    @Column(nullable = true)
    private UUID projekatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivoPrava nivo;

    @Column(nullable = false)
    private UUID dodeljivaoId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime datumDodele;
}
