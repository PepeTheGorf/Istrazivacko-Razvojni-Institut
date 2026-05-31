package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.TipDokumenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipDokumentaRepository extends JpaRepository<TipDokumenta, UUID> {
}
