package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.MetapodatakRequestDTO;
import org.example.documentmanagementservice.dto.MetapodatakResponseDTO;
import org.example.documentmanagementservice.exception.ResourceNotFoundException;
import org.example.documentmanagementservice.model.Metapodatak;
import org.example.documentmanagementservice.repository.MetapodatakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MetapodatakService {

    private final MetapodatakRepository repository;

    public List<MetapodatakResponseDTO> findAll() {
        return repository.findAll().stream().map(MetapodatakResponseDTO::fromEntity).toList();
    }

    public MetapodatakResponseDTO findById(UUID id) {
        return MetapodatakResponseDTO.fromEntity(findEntityById(id));
    }

    public MetapodatakResponseDTO create(MetapodatakRequestDTO request) {
        Metapodatak entity = Metapodatak.builder()
                .dokumentId(request.getDokumentId())
                .tipMetapodatkaId(request.getTipMetapodatkaId())
                .vrednost(request.getVrednost().trim())
                .build();
        return MetapodatakResponseDTO.fromEntity(repository.save(entity));
    }

    public MetapodatakResponseDTO update(UUID id, MetapodatakRequestDTO request) {
        Metapodatak entity = findEntityById(id);
        entity.setDokumentId(request.getDokumentId());
        entity.setTipMetapodatkaId(request.getTipMetapodatkaId());
        entity.setVrednost(request.getVrednost().trim());
        return MetapodatakResponseDTO.fromEntity(repository.save(entity));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Metapodatak not found: " + id);
        }
        repository.deleteById(id);
    }

    public List<MetapodatakResponseDTO> findByDokumentId(UUID dokumentId) {
        return repository.findByDokumentId(dokumentId).stream().map(MetapodatakResponseDTO::fromEntity).toList();
    }

    public void deleteByDokumentId(UUID dokumentId) {
        repository.deleteByDokumentId(dokumentId);
    }

    private Metapodatak findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metapodatak not found: " + id));
    }
}
