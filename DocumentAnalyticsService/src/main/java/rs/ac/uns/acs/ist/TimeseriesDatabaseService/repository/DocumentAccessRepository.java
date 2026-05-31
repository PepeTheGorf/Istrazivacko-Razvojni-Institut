package rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;

import java.util.List;

public interface DocumentAccessRepository {
    Boolean save(DocumentAccess access);
    List<DocumentAccess> findAll();
    List<DocumentAccess> findAllByUserId(String userId);
    List<DocumentAccess> findAllByDocumentId(String documentId);
    Boolean delete(String userId, String documentId, int days);
}
