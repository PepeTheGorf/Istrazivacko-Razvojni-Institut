package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.TipMetapodatkaRequestDTO;
import org.example.documentmanagementservice.dto.TipMetapodatkaResponseDTO;
import org.example.documentmanagementservice.model.TipMetapodatka;
import org.example.documentmanagementservice.repository.TipMetapodatkaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TipMetapodatkaService {

    private final TipMetapodatkaRepository repository;

    @Cacheable(value = "tip-metapodatka", key = "'all'")
    public List<TipMetapodatkaResponseDTO> findAll() {
        return repository.findAll().stream().map(TipMetapodatkaResponseDTO::fromEntity).toList();
    }

    @Cacheable(value = "tip-metapodatka", key = "#id")
    public TipMetapodatkaResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(TipMetapodatkaResponseDTO::fromEntity)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "TipMetapodatka not found: " + id));
    }

    @CacheEvict(value = "tip-metapodatka", allEntries = true)
    public TipMetapodatkaResponseDTO create(TipMetapodatkaRequestDTO request) {
        TipMetapodatka entity = TipMetapodatka.builder()
                .naziv(request.getNaziv().trim())
                .tipPodatka(request.getTipPodatka())
                .jeObavezan(request.isJeObavezan())
                .tipDokumentaId(request.getTipDokumentaId())
                .build();
        return TipMetapodatkaResponseDTO.fromEntity(repository.save(entity));
    }
}
