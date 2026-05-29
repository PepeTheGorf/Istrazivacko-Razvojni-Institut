package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.Metapodatak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MetapodatakRepository extends JpaRepository<Metapodatak, UUID> {

    List<Metapodatak> findByDokumentId(UUID dokumentId);

    List<Metapodatak> findByTipMetapodatkaId(UUID tipMetapodatkaId);

    List<Metapodatak> findByDokumentIdAndTipMetapodatkaId(UUID dokumentId, UUID tipMetapodatkaId);
}
