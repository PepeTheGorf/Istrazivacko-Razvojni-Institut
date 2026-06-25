package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.DokumentVerzija;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DokumentVerzijaRepository extends JpaRepository<DokumentVerzija, UUID> {

    List<DokumentVerzija> findByDokumentIdOrderByVerzijaBrojDesc(UUID dokumentId, Pageable pageable);

    Optional<DokumentVerzija> findTopByDokumentIdOrderByVerzijaBrojDesc(UUID dokumentId);

    int countByDokumentId(UUID dokumentId);
}
