package rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface DocumentAccessRepository {
    Boolean save(DocumentAccess access);
    List<DocumentAccess> findAll();
    List<DocumentAccess> findAllByUserId(String userId);
    List<DocumentAccess> findAllByDocumentId(String documentId);
    Boolean delete(String userId, String documentId, int days);
    List<DocumentAccess> findAllByTimeRange(Instant from, Instant to);
    List<DocumentAccess> findByDocumentIdAndTimeRange(String documentId, Instant from, Instant to);
    List<DocumentAccess> findByUserIdAndTimeRange(String userId, Instant from, Instant to);
    Map<String, Long> countByDocumentInTimeRange(Instant from, Instant to);
    Map<String, Long> countByUserInTimeRange(Instant from, Instant to);
}
