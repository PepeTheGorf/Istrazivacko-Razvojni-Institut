package rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentChange;

import java.util.List;
import java.util.Map;

public interface DocumentChangeRepository {
    Boolean save(DocumentChange change);
    List<DocumentChange> findAll();
    List<DocumentChange> findAllByDocumentId(String documentId);
    List<DocumentChange> findAllByChangeType(String changeType);
    Boolean delete(String documentId, String changeType, int days);
    List<Map<String, Object>> izmenePoTipuAkcije(String start, String stop);
    List<Map<String, Object>> dnevniTrendIzmena(String start, String stop);
}
