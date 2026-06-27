package rs.ac.uns.acs.ist.TimeseriesDatabaseService.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.service.DocumentAccessService;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.service.ReportPdfService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/document-access.json")
public class DocumentAccessController {

    private final DocumentAccessService service;
    private final ReportPdfService pdfService;

    public DocumentAccessController(DocumentAccessService service, ReportPdfService pdfService) {
        this.service = service;
        this.pdfService = pdfService;
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

    @GetMapping("report")
    public ResponseEntity<Map<String, Object>> report(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        List<DocumentAccess> accesses = service.findAllByTimeRange(fromTime, toTime);
        Map<String, Long> docCounts = service.topByDocumentInTimeRange(fromTime, toTime, 10);
        Map<String, Long> userCounts = service.topByUserInTimeRange(fromTime, toTime, 10);

        long uniqueUsers = accesses.stream().map(DocumentAccess::getUser_id).filter(u -> u != null).distinct().count();
        long uniqueDocs = accesses.stream().map(DocumentAccess::getDocument_id).filter(d -> d != null).distinct().count();

        List<Map<String, Object>> topDocuments = docCounts.entrySet().stream()
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("documentId", e.getKey()); m.put("accessCount", e.getValue()); return m; })
                .collect(Collectors.toList());
        List<Map<String, Object>> topUsers = userCounts.entrySet().stream()
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("userId", e.getKey()); m.put("accessCount", e.getValue()); return m; })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("from", fromTime.toString());
        result.put("to", toTime.toString());
        result.put("totalAccesses", accesses.size());
        result.put("uniqueUsers", uniqueUsers);
        result.put("uniqueDocuments", uniqueDocs);
        result.put("accessList", accesses);
        result.put("topDocuments", topDocuments);
        result.put("topUsers", topUsers);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("report/by-document")
    public ResponseEntity<Map<String, Object>> reportByDocument(
            @RequestParam("documentId") String documentId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        List<DocumentAccess> accesses = service.findByDocumentIdAndTimeRange(documentId, fromTime, toTime);
        Map<String, Long> userCounts = accesses.stream()
                .filter(a -> a.getUser_id() != null)
                .collect(Collectors.groupingBy(DocumentAccess::getUser_id, Collectors.counting()));

        long uniqueUsers = userCounts.size();

        List<Map<String, Object>> accessByUser = userCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("userId", e.getKey()); m.put("accessCount", e.getValue()); return m; })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("documentId", documentId);
        result.put("from", fromTime.toString());
        result.put("to", toTime.toString());
        result.put("totalAccesses", accesses.size());
        result.put("uniqueUsers", uniqueUsers);
        result.put("accesses", accesses);
        result.put("accessByUser", accessByUser);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("report/by-user")
    public ResponseEntity<Map<String, Object>> reportByUser(
            @RequestParam("userId") String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        List<DocumentAccess> accesses = service.findByUserIdAndTimeRange(userId, fromTime, toTime);
        Map<String, Long> docCounts = accesses.stream()
                .filter(a -> a.getDocument_id() != null)
                .collect(Collectors.groupingBy(DocumentAccess::getDocument_id, Collectors.counting()));

        long uniqueDocs = docCounts.size();

        List<Map<String, Object>> accessByDocument = docCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("documentId", e.getKey()); m.put("accessCount", e.getValue()); return m; })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("from", fromTime.toString());
        result.put("to", toTime.toString());
        result.put("totalAccesses", accesses.size());
        result.put("uniqueDocuments", uniqueDocs);
        result.put("accesses", accesses);
        result.put("accessByDocument", accessByDocument);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("stats/active-users")
    public ResponseEntity<List<Map<String, Object>>> activeUsers(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        Map<String, Long> top = service.topByUserInTimeRange(fromTime, toTime, 10);
        List<Map<String, Object>> result = top.entrySet().stream()
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("userId", e.getKey()); m.put("accessCount", e.getValue()); return m; })
                .collect(Collectors.toList());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("stats/active-documents")
    public ResponseEntity<List<Map<String, Object>>> activeDocuments(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        Map<String, Long> top = service.topByDocumentInTimeRange(fromTime, toTime, 10);
        List<Map<String, Object>> result = top.entrySet().stream()
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("documentId", e.getKey()); m.put("accessCount", e.getValue()); return m; })
                .collect(Collectors.toList());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "report/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> reportPdf(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "") String docNames,
            @RequestParam(required = false, defaultValue = "") String userNames) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        List<DocumentAccess> accesses = service.findAllByTimeRange(fromTime, toTime);
        Map<String, String> docMap = parseNameMap(docNames);
        Map<String, String> userMap = parseNameMap(userNames);

        byte[] pdf = pdfService.generateOverallReport(accesses, fromTime, toTime, docMap, userMap);
        return pdfResponse(pdf, "izvestaj-pristupa.pdf");
    }

    @GetMapping(value = "report/by-document/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> reportByDocumentPdf(
            @RequestParam("documentId") String documentId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "") String documentName,
            @RequestParam(required = false, defaultValue = "") String userNames) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        List<DocumentAccess> accesses = service.findByDocumentIdAndTimeRange(documentId, fromTime, toTime);
        String resolvedName = documentName.isBlank() ? documentId : documentName;
        Map<String, String> userMap = parseNameMap(userNames);

        byte[] pdf = pdfService.generateDocumentReport(accesses, documentId, resolvedName, fromTime, toTime, userMap);
        String filename = "izvestaj-dokument-" + resolvedName.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        return pdfResponse(pdf, filename);
    }

    @GetMapping(value = "report/by-user/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> reportByUserPdf(
            @RequestParam("userId") String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "") String userName,
            @RequestParam(required = false, defaultValue = "") String docNames) {
        Instant fromTime = from != null ? Instant.parse(from) : service.defaultFrom();
        Instant toTime = to != null ? Instant.parse(to) : service.defaultTo();

        List<DocumentAccess> accesses = service.findByUserIdAndTimeRange(userId, fromTime, toTime);
        String resolvedName = userName.isBlank() ? userId : userName;
        Map<String, String> docMap = parseNameMap(docNames);

        byte[] pdf = pdfService.generateUserReport(accesses, userId, resolvedName, fromTime, toTime, docMap);
        String filename = "izvestaj-korisnik-" + resolvedName.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        return pdfResponse(pdf, filename);
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    private Map<String, String> parseNameMap(String encoded) {
        Map<String, String> map = new HashMap<>();
        if (encoded == null || encoded.isBlank()) return map;
        for (String pair : encoded.split(",")) {
            String[] kv = pair.split("\\|", 2);
            if (kv.length == 2 && !kv[0].isBlank()) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }
}
