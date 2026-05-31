package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.PravaPristupa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PravaPristupaRepository extends JpaRepository<PravaPristupa, UUID> {

    List<PravaPristupa> findByKorisnikId(UUID korisnikId);

    List<PravaPristupa> findByDokumentId(UUID dokumentId);

    List<PravaPristupa> findByProjekatId(UUID projekatId);

    List<PravaPristupa> findByKorisnikIdAndDokumentId(UUID korisnikId, UUID dokumentId);
}
