package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.dto.PravaPristupaRequestDTO;
import org.example.documentmanagementservice.dto.PravaPristupaResponseDTO;
import org.example.documentmanagementservice.exception.ResourceNotFoundException;
import org.example.documentmanagementservice.model.PravaPristupa;
import org.example.documentmanagementservice.repository.PravaPristupaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PravaPristupaService {

    private final PravaPristupaRepository repository;

    public List<PravaPristupaResponseDTO> findAll() {
        return repository.findAll().stream().map(PravaPristupaResponseDTO::fromEntity).toList();
    }

    public PravaPristupaResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(PravaPristupaResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("PravaPristupa not found: " + id));
    }

    public PravaPristupaResponseDTO create(PravaPristupaRequestDTO request) {
        PravaPristupa entity = PravaPristupa.builder()
                .korisnikId(request.getKorisnikId())
                .dokumentId(request.getDokumentId())
                .folderId(request.getFolderId())
                .projekatId(request.getProjekatId())
                .nivo(request.getNivo())
                .dodeljivaoId(request.getDodeljivaoId())
                .build();
        return PravaPristupaResponseDTO.fromEntity(repository.save(entity));
    }

    public PravaPristupaResponseDTO update(UUID id, PravaPristupaRequestDTO request) {
        PravaPristupa entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PravaPristupa not found: " + id));
        entity.setKorisnikId(request.getKorisnikId());
        entity.setDokumentId(request.getDokumentId());
        entity.setFolderId(request.getFolderId());
        entity.setProjekatId(request.getProjekatId());
        entity.setNivo(request.getNivo());
        entity.setDodeljivaoId(request.getDodeljivaoId());
        return PravaPristupaResponseDTO.fromEntity(repository.save(entity));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("PravaPristupa not found: " + id);
        }
        repository.deleteById(id);
    }

    public List<PravaPristupaResponseDTO> findByKorisnikId(UUID korisnikId) {
        return repository.findByKorisnikId(korisnikId).stream().map(PravaPristupaResponseDTO::fromEntity).toList();
    }

    public List<PravaPristupaResponseDTO> findByDokumentId(UUID dokumentId) {
        return repository.findByDokumentId(dokumentId).stream().map(PravaPristupaResponseDTO::fromEntity).toList();
    }

    public List<PravaPristupaResponseDTO> findByProjekatId(UUID projekatId) {
        return repository.findByProjekatId(projekatId).stream().map(PravaPristupaResponseDTO::fromEntity).toList();
    }

    public PravaPristupaResponseDTO findByKorisnikAndDokument(UUID korisnikId, UUID dokumentId) {
        return repository.findByKorisnikIdAndDokumentId(korisnikId, dokumentId).stream()
            .findFirst()
            .map(PravaPristupaResponseDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("PravaPristupa not found for korisnik:" + korisnikId + " dokument:" + dokumentId));
    }
}
