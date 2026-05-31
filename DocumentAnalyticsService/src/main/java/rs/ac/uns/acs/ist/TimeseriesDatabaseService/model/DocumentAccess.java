package rs.ac.uns.acs.ist.TimeseriesDatabaseService.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "document_access")
public class DocumentAccess {

    @Column(tag = true)
    private String user_id;

    @Column(tag = true)
    private String document_id;

    @Column(tag = true)
    private String project_id;

    @Column(tag = true)
    private String action_type;

    @Column
    private Long session_duration_sec;

    @Column
    private Long file_size_bytes;

    @Column(timestamp = true)
    private Instant created;

    public DocumentAccess() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getAction_type() {
        return action_type;
    }

    public void setAction_type(String action_type) {
        this.action_type = action_type;
    }

    public Long getSession_duration_sec() {
        return session_duration_sec;
    }

    public void setSession_duration_sec(Long session_duration_sec) {
        this.session_duration_sec = session_duration_sec;
    }

    public Long getFile_size_bytes() {
        return file_size_bytes;
    }

    public void setFile_size_bytes(Long file_size_bytes) {
        this.file_size_bytes = file_size_bytes;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
