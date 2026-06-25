package org.example.documentmanagementservice.service;

import lombok.RequiredArgsConstructor;
import org.example.documentmanagementservice.model.Dokument;
import org.example.documentmanagementservice.model.DokumentVerzija;
import org.example.documentmanagementservice.repository.DokumentRepository;
import org.example.documentmanagementservice.repository.DokumentVerzijaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DokumentVerzijaService {

    private final DokumentVerzijaRepository dokumentVerzijaRepository;
    private final DokumentRepository dokumentRepository;

    @Transactional
    public DokumentVerzija createVerzija(Dokument trenutni, UUID sacuvaoId) {
        int maxBroj = dokumentVerzijaRepository.findTopByDokumentIdOrderByVerzijaBrojDesc(trenutni.getId())
                .map(DokumentVerzija::getVerzijaBroj)
                .orElse(0);

        DokumentVerzija verzija = DokumentVerzija.builder()
                .dokumentId(trenutni.getId())
                .verzijaBroj(maxBroj + 1)
                .naslov(trenutni.getNaslov())
                .sadrzaj(trenutni.getSadrzaj())
                .sacuvaoId(sacuvaoId)
                .datumKreiranja(Instant.now())
                .build();

        return dokumentVerzijaRepository.save(verzija);
    }

    public List<DokumentVerzija> getHistory(UUID dokumentId) {
        return dokumentVerzijaRepository.findByDokumentIdOrderByVerzijaBrojDesc(
                dokumentId, PageRequest.of(0, 50));
    }

    @Transactional
    public Dokument restore(UUID dokumentId, UUID verzijaId, UUID korisnikId) {
        DokumentVerzija verzija = dokumentVerzijaRepository.findById(verzijaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verzija not found"));

        if (!verzija.getDokumentId().equals(dokumentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verzija does not belong to this document");
        }

        Dokument dokument = dokumentRepository.findById(dokumentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dokument not found"));

        createVerzija(dokument, korisnikId);

        dokument.setNaslov(verzija.getNaslov());
        dokument.setSadrzaj(verzija.getSadrzaj());

        return dokumentRepository.save(dokument);
    }
}
