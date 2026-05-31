package rs.ac.uns.acs.ist.TimeseriesDatabaseService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.service.DocumentAccessService;

import java.util.List;

@RestController
@RequestMapping("/document-access.json")
public class DocumentAccessController {

    private final DocumentAccessService service;

    public DocumentAccessController(DocumentAccessService service) {
        this.service = service;
    }

    @GetMapping("findAll")
    public ResponseEntity<List<DocumentAccess>> findAll() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("findAllByUserId")
    public ResponseEntity<List<DocumentAccess>> findAllByUserId(@RequestParam("userId") String userId) {
        return new ResponseEntity<>(service.findAllByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("findAllByDocumentId")
    public ResponseEntity<List<DocumentAccess>> findAllByDocumentId(@RequestParam("documentId") String documentId) {
        return new ResponseEntity<>(service.findAllByDocumentId(documentId), HttpStatus.OK);
    }

    @PostMapping("save")
    public ResponseEntity<Boolean> save(@RequestBody DocumentAccess access) {
        return service.save(access) ? new ResponseEntity<>(true, HttpStatus.OK) : new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("delete")
    public ResponseEntity<Boolean> delete(@RequestParam("userId") String userId, @RequestParam("documentId") String documentId, @RequestParam(defaultValue = "7") int days) {
        return service.delete(userId, documentId, days) ? new ResponseEntity<>(true, HttpStatus.OK) : new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
}
