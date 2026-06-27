package rs.ac.uns.acs.ist.TimeseriesDatabaseService.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository.DocumentAccessRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentAccessService {

    private final DocumentAccessRepository repository;

    public DocumentAccessService(DocumentAccessRepository repository) {
        this.repository = repository;
    }

    public Boolean save(DocumentAccess access) {
        return repository.save(access);
    }

    public List<DocumentAccess> findAll() {
        return repository.findAll();
    }

    public List<DocumentAccess> findAllByUserId(String userId) {
        return repository.findAllByUserId(userId);
    }

    public List<DocumentAccess> findAllByDocumentId(String documentId) {
        return repository.findAllByDocumentId(documentId);
    }

    public Boolean delete(String userId, String documentId, int days) {
        return repository.delete(userId, documentId, days);
    }

    public List<DocumentAccess> findAllByTimeRange(Instant from, Instant to) {
        return repository.findAllByTimeRange(from, to);
    }

    public List<DocumentAccess> findByDocumentIdAndTimeRange(String documentId, Instant from, Instant to) {
        return repository.findByDocumentIdAndTimeRange(documentId, from, to);
    }

    public List<DocumentAccess> findByUserIdAndTimeRange(String userId, Instant from, Instant to) {
        return repository.findByUserIdAndTimeRange(userId, from, to);
    }

    public Map<String, Long> countByDocumentInTimeRange(Instant from, Instant to) {
        return repository.countByDocumentInTimeRange(from, to);
    }

    public Map<String, Long> countByUserInTimeRange(Instant from, Instant to) {
        return repository.countByUserInTimeRange(from, to);
    }

    public Map<String, Long> topByDocumentInTimeRange(Instant from, Instant to, int limit) {
        return repository.countByDocumentInTimeRange(from, to).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public Map<String, Long> topByUserInTimeRange(Instant from, Instant to, int limit) {
        return repository.countByUserInTimeRange(from, to).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public Instant defaultFrom() {
        return Instant.now().minus(30, ChronoUnit.DAYS);
    }

    public Instant defaultTo() {
        return Instant.now();
    }
}
