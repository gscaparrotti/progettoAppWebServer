package application.entities;

import javax.persistence.*;

@Entity
public class DBFile {
    @Id
    private String fileName;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
