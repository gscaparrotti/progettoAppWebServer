package application.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="request_id", nullable = false)
    private LegalAssistance request;
    @Column(nullable = false)
    private boolean fromUser;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Column(nullable = false)
    private Date date;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LegalAssistance getRequest() {
        return request;
    }

    public void setRequest(LegalAssistance request) {
        this.request = request;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
