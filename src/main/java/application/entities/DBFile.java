package application.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class DBFile {

    @EmbeddedId
    private DBFileID dbFileID;
    @JsonIgnore
    @MapsId("request")
    @ManyToOne
    @JoinColumn(name="legal_assistance_id", nullable=false)
    private LegalAssistance request;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] data;

    public LegalAssistance getRequest() {
        return request;
    }

    public void setRequest(LegalAssistance request) {
        this.request = request;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @JsonProperty("id")
    public DBFileID getDbFileID() {
        return dbFileID;
    }

    public void setDbFileID(DBFileID dbFileID) {
        this.dbFileID = dbFileID;
    }

    @Embeddable
    public static class DBFileID implements Serializable {

        private String filename;
        //this must be of the same type of the key in LegalRequestController
        private long request;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public long getRequest() {
            return request;
        }

        public void setRequest(long request) {
            this.request = request;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DBFileID dbFileID = (DBFileID) o;
            return request == dbFileID.request &&
                    Objects.equals(filename, dbFileID.filename);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filename, request);
        }
    }
}
