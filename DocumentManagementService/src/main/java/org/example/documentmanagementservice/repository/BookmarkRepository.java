package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    List<Bookmark> findByKorisnikId(UUID korisnikId);

    Optional<Bookmark> findByKorisnikIdAndDokumentId(UUID korisnikId, UUID dokumentId);

    boolean existsByKorisnikIdAndDokumentId(UUID korisnikId, UUID dokumentId);
}
