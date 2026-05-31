package rs.ac.uns.acs.ist.TimeseriesDatabaseService.configuration;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentAccess;
import rs.ac.uns.acs.ist.TimeseriesDatabaseService.model.DocumentChange;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class InfluxDBConnectionClass {

    @Value("${spring.influx.token}")
    private String token;

    @Value("${spring.influx.bucket}")
    private String bucket;

    @Value("${spring.influx.org}")
    private String org;

    @Value("${spring.influx.url}")
    private String url;

    public InfluxDBClient buildConnection() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }

    public boolean saveAccess(InfluxDBClient client, DocumentAccess access) {
        return writeMeasurement(client, access);
    }

    public boolean saveChange(InfluxDBClient client, DocumentChange change) {
        return writeMeasurement(client, change);
    }

    public List<DocumentAccess> findAllAccess(InfluxDBClient client) {
        String flux = "from(bucket:\"" + bucket + "\") |> range(start: 0) |> filter(fn: (r) => r[\"_measurement\"] == \"document_access\") |> sort(columns: [\"_time\"], desc: true)";
        return mapAccess(client.getQueryApi(), flux);
    }

    public List<DocumentAccess> findAccessByUserId(InfluxDBClient client, String userId) {
        String flux = "from(bucket:\"" + bucket + "\") |> range(start: 0) |> filter(fn: (r) => r[\"_measurement\"] == \"document_access\" and r[\"user_id\"] == \"" + userId + "\") |> sort(columns: [\"_time\"], desc: true)";
        return mapAccess(client.getQueryApi(), flux);
    }

    public List<DocumentAccess> findAccessByDocumentId(InfluxDBClient client, String documentId) {
        String flux = "from(bucket:\"" + bucket + "\") |> range(start: 0) |> filter(fn: (r) => r[\"_measurement\"] == \"document_access\" and r[\"document_id\"] == \"" + documentId + "\") |> sort(columns: [\"_time\"], desc: true)";
        return mapAccess(client.getQueryApi(), flux);
    }

    public List<DocumentChange> findAllChanges(InfluxDBClient client) {
        String flux = "from(bucket:\"" + bucket + "\") |> range(start: 0) |> filter(fn: (r) => r[\"_measurement\"] == \"document_changes\") |> sort(columns: [\"_time\"], desc: true)";
        return mapChange(client.getQueryApi(), flux);
    }

    public List<DocumentChange> findChangesByDocumentId(InfluxDBClient client, String documentId) {
        String flux = "from(bucket:\"" + bucket + "\") |> range(start: 0) |> filter(fn: (r) => r[\"_measurement\"] == \"document_changes\" and r[\"document_id\"] == \"" + documentId + "\") |> sort(columns: [\"_time\"], desc: true)";
        return mapChange(client.getQueryApi(), flux);
    }

    public List<DocumentChange> findChangesByChangeType(InfluxDBClient client, String changeType) {
        String flux = "from(bucket:\"" + bucket + "\") |> range(start: 0) |> filter(fn: (r) => r[\"_measurement\"] == \"document_changes\" and r[\"change_type\"] == \"" + changeType + "\") |> sort(columns: [\"_time\"], desc: true)";
        return mapChange(client.getQueryApi(), flux);
    }

    public boolean deleteByPredicate(InfluxDBClient client, String measurement, String predicate, OffsetDateTime start, OffsetDateTime stop) {
        try {
            DeleteApi deleteApi = client.getDeleteApi();
            deleteApi.delete(start, stop, "_measurement=\"" + measurement + "\" AND (" + predicate + ")", bucket, org);
            return true;
        } catch (InfluxException e) {
            return false;
        }
    }

    private boolean writeMeasurement(InfluxDBClient client, Object measurement) {
        try {
            WriteApiBlocking writeApi = client.getWriteApiBlocking();
            writeApi.writeMeasurement(WritePrecision.MS, measurement);
            return true;
        } catch (InfluxException e) {
            return false;
        }
    }

    private List<DocumentAccess> mapAccess(QueryApi queryApi, String flux) {
        List<DocumentAccess> values = new ArrayList<>();
        for (FluxTable table : queryApi.query(flux)) {
            for (FluxRecord record : table.getRecords()) {
                DocumentAccess access = new DocumentAccess();
                access.setUser_id((String) record.getValueByKey("user_id"));
                access.setDocument_id((String) record.getValueByKey("document_id"));
                access.setProject_id((String) record.getValueByKey("project_id"));
                access.setAction_type((String) record.getValueByKey("action_type"));
                access.setSession_duration_sec(toLong(record.getValueByKey("session_duration_sec")));
                access.setFile_size_bytes(toLong(record.getValueByKey("file_size_bytes")));
                access.setCreated((Instant) record.getValueByKey("_time"));
                values.add(access);
            }
        }
        return values;
    }

    private List<DocumentChange> mapChange(QueryApi queryApi, String flux) {
        List<DocumentChange> values = new ArrayList<>();
        for (FluxTable table : queryApi.query(flux)) {
            for (FluxRecord record : table.getRecords()) {
                DocumentChange change = new DocumentChange();
                change.setUser_id((String) record.getValueByKey("user_id"));
                change.setDocument_id((String) record.getValueByKey("document_id"));
                change.setProject_id((String) record.getValueByKey("project_id"));
                change.setChange_type((String) record.getValueByKey("change_type"));
                change.setVersion_number(toInteger(record.getValueByKey("version_number")));
                change.setIs_content_change(toInteger(record.getValueByKey("is_content_change")));
                change.setIs_metadata_change(toInteger(record.getValueByKey("is_metadata_change")));
                change.setCreated((Instant) record.getValueByKey("_time"));
                values.add(change);
            }
        }
        return values;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
