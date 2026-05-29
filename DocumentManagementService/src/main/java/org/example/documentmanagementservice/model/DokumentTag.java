package org.example.documentmanagementservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dokument_tag")
@IdClass(DokumentTagId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DokumentTag {

    @Id
    @Column(nullable = false)
    private UUID dokumentId;

    @Id
    @Column(nullable = false)
    private UUID tagId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
