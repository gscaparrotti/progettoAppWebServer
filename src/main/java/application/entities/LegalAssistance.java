package application.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "className")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class LegalAssistance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
    @JsonIgnore
    @OneToMany(mappedBy = "request")
    private Set<DBFile> files;
    @JsonIgnore
    @OneToMany(mappedBy = "request")
    private Set<Message> messages;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date requestDate;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date paymentDate;
    private PaymentType paymentType;

    public Set<DBFile> getFiles() {
        return files;
    }

    public void setFiles(Set<DBFile> files) {
        this.files = files;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public enum PaymentType {
        CREDIT_CARD, BANK_TRANSFER
    }
}
