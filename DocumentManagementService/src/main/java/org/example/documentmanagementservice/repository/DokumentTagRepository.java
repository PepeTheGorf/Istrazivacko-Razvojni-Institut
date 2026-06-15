package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.DokumentTag;
import org.example.documentmanagementservice.model.DokumentTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DokumentTagRepository extends JpaRepository<DokumentTag, DokumentTagId> {

    List<DokumentTag> findByDokumentId(UUID dokumentId);

    List<DokumentTag> findByTagId(UUID tagId);

    void deleteByDokumentId(UUID dokumentId);
}
