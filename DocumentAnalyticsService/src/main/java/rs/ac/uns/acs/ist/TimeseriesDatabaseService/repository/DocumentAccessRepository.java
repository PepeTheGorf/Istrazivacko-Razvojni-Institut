package rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;

import java.util.List;
import java.util.Map;

public interface DocumentAccessRepository {
    Boolean save(DocumentAccess access);
    List<DocumentAccess> findAll();
    List<DocumentAccess> findAllByUserId(String userId);
    List<DocumentAccess> findAllByDocumentId(String documentId);
    Boolean delete(String userId, String documentId, int days);
    List<Map<String, Object>> najaktivnijiKorisnici(String start, String stop, int limit);
    List<Map<String, Object>> topDokumentiPoPregledu(String start, String stop, int limit);
    List<Map<String, Object>> trendPristupaDokumentima(String start, String stop);
}
