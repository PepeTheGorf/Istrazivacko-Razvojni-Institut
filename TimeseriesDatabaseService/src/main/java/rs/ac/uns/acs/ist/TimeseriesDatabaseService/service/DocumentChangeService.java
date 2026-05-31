package rs.ac.uns.acs.ist.TimeseriesDatabaseService.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentChange;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository.DocumentChangeRepository;

import java.util.List;

@Service
public class DocumentChangeService {

    private final DocumentChangeRepository repository;

    public DocumentChangeService(DocumentChangeRepository repository) {
        this.repository = repository;
    }

    public Boolean save(DocumentChange change) {
        return repository.save(change);
    }

    public List<DocumentChange> findAll() {
        return repository.findAll();
    }

    public List<DocumentChange> findAllByDocumentId(String documentId) {
        return repository.findAllByDocumentId(documentId);
    }

    public List<DocumentChange> findAllByChangeType(String changeType) {
        return repository.findAllByChangeType(changeType);
    }

    public Boolean delete(String documentId, String changeType, int days) {
        return repository.delete(documentId, changeType, days);
    }
}
