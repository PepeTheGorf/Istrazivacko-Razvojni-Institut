package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.TipDokumentaRequestDTO;
import org.example.documentmanagementservice.dto.TipDokumentaResponseDTO;
import org.example.documentmanagementservice.exception.ResourceNotFoundException;
import org.example.documentmanagementservice.model.TipDokumenta;
import org.example.documentmanagementservice.repository.TipDokumentaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TipDokumentaService {

    private final TipDokumentaRepository repository;

    @Cacheable(value = "tip-dokumenta", key = "'all'")
    public List<TipDokumentaResponseDTO> findAll() {
        return repository.findAll().stream().map(TipDokumentaResponseDTO::fromEntity).toList();
    }

    @Cacheable(value = "tip-dokumenta", key = "#id")
    public TipDokumentaResponseDTO findById(UUID id) {
        return TipDokumentaResponseDTO.fromEntity(findEntityById(id));
    }

    @CacheEvict(value = "tip-dokumenta", allEntries = true)
    public TipDokumentaResponseDTO create(TipDokumentaRequestDTO request) {
        TipDokumenta entity = TipDokumenta.builder()
                .naziv(request.getNaziv().trim())
                .build();
        return TipDokumentaResponseDTO.fromEntity(repository.save(entity));
    }

    @CacheEvict(value = "tip-dokumenta", allEntries = true)
    public TipDokumentaResponseDTO update(UUID id, TipDokumentaRequestDTO request) {
        TipDokumenta entity = findEntityById(id);
        entity.setNaziv(request.getNaziv().trim());
        return TipDokumentaResponseDTO.fromEntity(repository.save(entity));
    }

    @CacheEvict(value = "tip-dokumenta", allEntries = true)
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TipDokumenta not found: " + id);
        }
        repository.deleteById(id);
    }

    private TipDokumenta findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipDokumenta not found: " + id));
    }
}
