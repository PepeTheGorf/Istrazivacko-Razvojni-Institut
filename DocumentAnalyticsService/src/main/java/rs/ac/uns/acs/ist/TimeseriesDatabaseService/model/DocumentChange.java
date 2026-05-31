package rs.ac.uns.acs.ist.TimeseriesDatabaseService.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "document_changes")
public class DocumentChange {

    @Column(tag = true)
    private String user_id;

    @Column(tag = true)
    private String document_id;

    @Column(tag = true)
    private String project_id;

    @Column(tag = true)
    private String change_type;

    @Column
    private Integer version_number;

    @Column
    private Integer is_content_change;

    @Column
    private Integer is_metadata_change;

    @Column(timestamp = true)
    private Instant created;

    public DocumentChange() {
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

    public String getChange_type() {
        return change_type;
    }

    public void setChange_type(String change_type) {
        this.change_type = change_type;
    }

    public Integer getVersion_number() {
        return version_number;
    }

    public void setVersion_number(Integer version_number) {
        this.version_number = version_number;
    }

    public Integer getIs_content_change() {
        return is_content_change;
    }

    public void setIs_content_change(Integer is_content_change) {
        this.is_content_change = is_content_change;
    }

    public Integer getIs_metadata_change() {
        return is_metadata_change;
    }

    public void setIs_metadata_change(Integer is_metadata_change) {
        this.is_metadata_change = is_metadata_change;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
