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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> najaktivnijiKorisnici(InfluxDBClient client, String start, String stop, int limit) {
        String flux = "from(bucket:\"" + bucket + "\")\n" +
                "  |> range(start: " + start + ", stop: " + stop + ")\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"document_access\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"session_duration_sec\")\n" +
                "  |> group(columns: [\"user_id\"])\n" +
                "  |> count()\n" +
                "  |> group()\n" +
                "  |> sort(columns: [\"_value\"], desc: true)\n" +
                "  |> limit(n: " + limit + ")\n" +
                "  |> rename(columns: {_value: \"total_accesses\"})";
        return mapToKeyValue(client.getQueryApi(), flux, "user_id", "total_accesses");
    }

    public List<Map<String, Object>> topDokumentiPoPregledu(InfluxDBClient client, String start, String stop, int limit) {
        String flux = "from(bucket:\"" + bucket + "\")\n" +
                "  |> range(start: " + start + ", stop: " + stop + ")\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"document_access\")\n" +
                "  |> filter(fn: (r) => r[\"action_type\"] == \"pregled\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"session_duration_sec\")\n" +
                "  |> group(columns: [\"document_id\"])\n" +
                "  |> count()\n" +
                "  |> group()\n" +
                "  |> rename(columns: {_value: \"view_count\"})\n" +
                "  |> sort(columns: [\"view_count\"], desc: true)\n" +
                "  |> limit(n: " + limit + ")";
        return mapToKeyValue(client.getQueryApi(), flux, "document_id", "view_count");
    }

    public List<Map<String, Object>> izmenePoTipuAkcije(InfluxDBClient client, String start, String stop) {
        String flux = "from(bucket:\"" + bucket + "\")\n" +
                "  |> range(start: " + start + ", stop: " + stop + ")\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"document_changes\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"version_number\")\n" +
                "  |> group(columns: [\"change_type\"])\n" +
                "  |> count()\n" +
                "  |> group()\n" +
                "  |> rename(columns: {_value: \"broj_izmena\"})\n" +
                "  |> sort(columns: [\"broj_izmena\"], desc: true)";
        return mapToKeyValue(client.getQueryApi(), flux, "change_type", "broj_izmena");
    }

    public List<Map<String, Object>> dnevniTrendIzmena(InfluxDBClient client, String start, String stop) {
        String flux = "from(bucket:\"" + bucket + "\")\n" +
                "  |> range(start: " + start + ", stop: " + stop + ")\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"document_changes\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"version_number\")\n" +
                "  |> map(fn: (r) => ({r with _field: \"Broj izmena\"}))\n" +
                "  |> group(columns: [\"_field\"])\n" +
                "  |> aggregateWindow(every: 1d, fn: count, createEmpty: true)\n" +
                "  |> yield(name: \"dnevne_izmene\")";
        return mapTimeSeries(client.getQueryApi(), flux);
    }

    public List<Map<String, Object>> trendPristupaDokumentima(InfluxDBClient client, String start, String stop) {
        String flux = "from(bucket:\"" + bucket + "\")\n" +
                "  |> range(start: " + start + ", stop: " + stop + ")\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"document_access\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"session_duration_sec\")\n" +
                "  |> map(fn: (r) => ({r with _field: \"Broj pristupa\"}))\n" +
                "  |> group(columns: [\"_field\"])\n" +
                "  |> aggregateWindow(every: 1d, fn: count, createEmpty: true)\n" +
                "  |> yield(name: \"count\")";
        return mapTimeSeries(client.getQueryApi(), flux);
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

    private List<Map<String, Object>> mapToKeyValue(QueryApi queryApi, String flux, String keyColumn, String valueColumn) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (FluxTable table : queryApi.query(flux)) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(keyColumn, record.getValueByKey(keyColumn));
                Object val = record.getValueByKey(valueColumn);
                if (val == null) val = record.getValue();
                row.put(valueColumn, val);
                result.add(row);
            }
        }
        return result;
    }

    private List<Map<String, Object>> mapTimeSeries(QueryApi queryApi, String flux) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (FluxTable table : queryApi.query(flux)) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("time", record.getTime());
                row.put("value", record.getValue());
                result.add(row);
            }
        }
        return result;
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
