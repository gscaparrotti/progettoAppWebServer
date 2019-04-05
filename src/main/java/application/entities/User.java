package application.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Set;

@Entity
public class User {

    @Id
    @Column(length = 16)
    private String codicefiscale;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<LegalAssistance> requiredLegalAssistance;

    public String getCodicefiscale() {
        return codicefiscale;
    }

    public void setCodicefiscale(String codicefiscale) {
        this.codicefiscale = codicefiscale;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<LegalAssistance> getRequiredLegalAssistance() {
        return requiredLegalAssistance;
    }

    public void setRequiredLegalAssistance(Set<LegalAssistance> requiredLegalAssistance) {
        this.requiredLegalAssistance = requiredLegalAssistance;
    }
}
