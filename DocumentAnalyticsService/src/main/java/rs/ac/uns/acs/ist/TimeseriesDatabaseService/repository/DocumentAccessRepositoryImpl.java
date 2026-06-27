package rs.ac.uns.acs.ist.TimeseriesDatabaseService.repository;

import com.influxdb.client.InfluxDBClient;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.configuration.InfluxDBConnectionClass;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentAccessRepositoryImpl implements DocumentAccessRepository {

    private final InfluxDBConnectionClass inConn;

    public DocumentAccessRepositoryImpl(InfluxDBConnectionClass inConn) {
        this.inConn = inConn;
    }

    @Override
    public Boolean save(DocumentAccess access) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.saveAccess(client, access);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentAccess> findAll() {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findAllAccess(client);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentAccess> findAllByUserId(String userId) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findAccessByUserId(client, userId);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentAccess> findAllByDocumentId(String documentId) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findAccessByDocumentId(client, documentId);
        } finally {
            client.close();
        }
    }

    @Override
    public Boolean delete(String userId, String documentId, int days) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            OffsetDateTime stop = OffsetDateTime.now();
            OffsetDateTime start = stop.minusDays(days);
            String predicate = "user_id=\"" + userId + "\" AND document_id=\"" + documentId + "\"";
            return inConn.deleteByPredicate(client, "document_access", predicate, start, stop);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentAccess> findAllByTimeRange(Instant from, Instant to) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findAccessByTimeRange(client, from, to);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentAccess> findByDocumentIdAndTimeRange(String documentId, Instant from, Instant to) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findAccessByDocumentIdAndTimeRange(client, documentId, from, to);
        } finally {
            client.close();
        }
    }

    @Override
    public List<DocumentAccess> findByUserIdAndTimeRange(String userId, Instant from, Instant to) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.findAccessByUserIdAndTimeRange(client, userId, from, to);
        } finally {
            client.close();
        }
    }

    @Override
    public Map<String, Long> countByDocumentInTimeRange(Instant from, Instant to) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.countAccessByDocumentInTimeRange(client, from, to);
        } finally {
            client.close();
        }
    }

    @Override
    public Map<String, Long> countByUserInTimeRange(Instant from, Instant to) {
        InfluxDBClient client = inConn.buildConnection();
        try {
            return inConn.countAccessByUserInTimeRange(client, from, to);
        } finally {
            client.close();
        }
    }
}
