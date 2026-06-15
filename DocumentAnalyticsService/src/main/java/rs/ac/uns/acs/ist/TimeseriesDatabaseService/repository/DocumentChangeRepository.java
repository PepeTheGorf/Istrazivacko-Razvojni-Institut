package rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentChange;

import java.util.List;

public interface DocumentChangeRepository {
    Boolean save(DocumentChange change);
    List<DocumentChange> findAll();
    List<DocumentChange> findAllByDocumentId(String documentId);
    List<DocumentChange> findAllByChangeType(String changeType);
    Boolean delete(String documentId, String changeType, int days);
}
