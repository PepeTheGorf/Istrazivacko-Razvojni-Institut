package rs.ac.uns.acs.ist.TimeseriesDatabaseService.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository.DocumentAccessRepository;

import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> najaktivnijiKorisnici(String start, String stop, int limit) {
        return repository.najaktivnijiKorisnici(start, stop, limit);
    }

    public List<Map<String, Object>> topDokumentiPoPregledu(String start, String stop, int limit) {
        return repository.topDokumentiPoPregledu(start, stop, limit);
    }

    public List<Map<String, Object>> trendPristupaDokumentima(String start, String stop) {
        return repository.trendPristupaDokumentima(start, stop);
    }
}
