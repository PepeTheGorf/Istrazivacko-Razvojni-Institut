package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.BookmarkRequestDTO;
import org.example.documentmanagementservice.dto.BookmarkResponseDTO;
import org.example.documentmanagementservice.exception.ResourceNotFoundException;
import org.example.documentmanagementservice.model.Bookmark;
import org.example.documentmanagementservice.repository.BookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository repository;

    public List<BookmarkResponseDTO> findAll() {
        return repository.findAll().stream().map(BookmarkResponseDTO::fromEntity).toList();
    }

    public BookmarkResponseDTO findById(UUID id) {
        return BookmarkResponseDTO.fromEntity(findEntityById(id));
    }

    public BookmarkResponseDTO create(BookmarkRequestDTO request) {
        Bookmark entity = Bookmark.builder()
                .korisnikId(request.getKorisnikId())
                .dokumentId(request.getDokumentId())
                .build();
        return BookmarkResponseDTO.fromEntity(repository.save(entity));
    }

    public BookmarkResponseDTO update(UUID id, BookmarkRequestDTO request) {
        Bookmark entity = findEntityById(id);
        entity.setKorisnikId(request.getKorisnikId());
        entity.setDokumentId(request.getDokumentId());
        return BookmarkResponseDTO.fromEntity(repository.save(entity));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Bookmark not found: " + id);
        }
        repository.deleteById(id);
    }

    private Bookmark findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found: " + id));
    }
}
