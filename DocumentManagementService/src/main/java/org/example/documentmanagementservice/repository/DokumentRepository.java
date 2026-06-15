package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.Dokument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DokumentRepository extends JpaRepository<Dokument, UUID> {
}
