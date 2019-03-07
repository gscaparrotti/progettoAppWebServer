package application.model;

import javax.persistence.Entity;

@Entity
public class DrunkDriving extends LegalAssistance {

    private double rilevazione;
    private boolean recidiva;
    private boolean rifiutato;
    private boolean estraneo;
    private boolean incidente;

    public double getRilevazione() {
        return rilevazione;
    }

    public void setRilevazione(double rilevazione) {
        this.rilevazione = rilevazione;
    }

    public boolean isRecidiva() {
        return recidiva;
    }

    public void setRecidiva(boolean recidiva) {
        this.recidiva = recidiva;
    }

    public boolean isRifiutato() {
        return rifiutato;
    }

    public void setRifiutato(boolean rifiutato) {
        this.rifiutato = rifiutato;
    }

    public boolean isEstraneo() {
        return estraneo;
    }

    public void setEstraneo(boolean estraneo) {
        this.estraneo = estraneo;
    }

    public boolean isIncidente() {
        return incidente;
    }

    public void setIncidente(boolean incidente) {
        this.incidente = incidente;
    }
}
