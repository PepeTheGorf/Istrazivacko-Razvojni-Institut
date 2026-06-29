package rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository;

import com.influxdb.client.InfluxDBClient;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.configuration.InfluxDBConnectionClass;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentChange;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentChangeRepositoryImpl implements DocumentChangeRepository {

    private final InfluxDBConnectionClass inConn;

    public DocumentChangeRepositoryImpl(InfluxDBConnectionClass inConn) {
        this.inConn = inConn;
    }

    @Override
    public Boolean save(DocumentChange change) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.saveChange(client, change);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentChange> findAll() {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findAllChanges(client);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentChange> findAllByDocumentId(String documentId) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findChangesByDocumentId(client, documentId);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentChange> findAllByChangeType(String changeType) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findChangesByChangeType(client, changeType);
        } finally {
            client.close();
        }
    }

    @Override
    public Boolean delete(String documentId, String changeType, int days) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            OffsetDateTime stop = OffsetDateTime.now();
            OffsetDateTime start = stop.minusDays(days);
            String predicate = "document_id=\"" + documentId + "\" AND change_type=\"" + changeType + "\"";
            return inConn.deleteByPredicate(client, "document_changes", predicate, start, stop);
        } finally {
            client.close();
        }
    }

    @Override
    public List<Map<String, Object>> izmenePoTipuAkcije(String start, String stop) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.izmenePoTipuAkcije(client, start, stop);
        } finally {
            client.close();
        }
    }

    @Override
    public List<Map<String, Object>> dnevniTrendIzmena(String start, String stop) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.dnevniTrendIzmena(client, start, stop);
        } finally {
            client.close();
        }
    }
}
