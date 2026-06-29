package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.TipMetapodatka;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipMetapodatkaRepository extends JpaRepository<TipMetapodatka, UUID> {
}
