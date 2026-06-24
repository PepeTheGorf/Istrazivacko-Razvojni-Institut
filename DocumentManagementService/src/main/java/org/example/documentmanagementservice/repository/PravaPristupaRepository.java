package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.PravaPristupa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PravaPristupaRepository extends JpaRepository<PravaPristupa, UUID> {

    List<PravaPristupa> findByKorisnikId(UUID korisnikId);

    List<PravaPristupa> findByDokumentId(UUID dokumentId);

    List<PravaPristupa> findByProjekatId(UUID projekatId);

    List<PravaPristupa> findByKorisnikIdAndDokumentId(UUID korisnikId, UUID dokumentId);

    Optional<PravaPristupa> findFirstByKorisnikIdAndDokumentId(UUID korisnikId, UUID dokumentId);

    Optional<PravaPristupa> findFirstByKorisnikIdAndProjekatId(UUID korisnikId, UUID projekatId);

    @Transactional
    void deleteByKorisnikIdAndDokumentId(UUID korisnikId, UUID dokumentId);

    @Transactional
    void deleteByKorisnikIdAndProjekatId(UUID korisnikId, UUID projekatId);
}
