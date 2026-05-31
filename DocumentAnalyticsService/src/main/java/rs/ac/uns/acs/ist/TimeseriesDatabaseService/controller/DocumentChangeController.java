package rs.ac.uns.acs.ist.TimeseriesDatabaseService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentChange;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.service.DocumentChangeService;

import java.util.List;

@RestController
@RequestMapping("/document-change.json")
public class DocumentChangeController {

    private final DocumentChangeService service;

    public DocumentChangeController(DocumentChangeService service) {
        this.service = service;
    }

    @GetMapping("findAll")
    public ResponseEntity<List<DocumentChange>> findAll() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("findAllByDocumentId")
    public ResponseEntity<List<DocumentChange>> findAllByDocumentId(@RequestParam("documentId") String documentId) {
        return new ResponseEntity<>(service.findAllByDocumentId(documentId), HttpStatus.OK);
    }

    @GetMapping("findAllByChangeType")
    public ResponseEntity<List<DocumentChange>> findAllByChangeType(@RequestParam("changeType") String changeType) {
        return new ResponseEntity<>(service.findAllByChangeType(changeType), HttpStatus.OK);
    }

    @PostMapping("save")
    public ResponseEntity<Boolean> save(@RequestBody DocumentChange change) {
        return service.save(change) ? new ResponseEntity<>(true, HttpStatus.OK) : new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("delete")
    public ResponseEntity<Boolean> delete(@RequestParam("documentId") String documentId, @RequestParam("changeType") String changeType, @RequestParam(defaultValue = "7") int days) {
        return service.delete(documentId, changeType, days) ? new ResponseEntity<>(true, HttpStatus.OK) : new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
}
