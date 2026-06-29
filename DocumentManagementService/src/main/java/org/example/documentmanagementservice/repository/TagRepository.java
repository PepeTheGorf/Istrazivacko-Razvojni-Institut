package org.example.documentmanagementservice.repository;

import org.example.documentmanagementservice.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByNazivContainingIgnoreCase(String naziv);

    Optional<Tag> findByNaziv(String naziv);
}
