package application.entities;

import javax.persistence.Entity;

@Entity
public class DrunkDriving extends LegalAssistance {

    private double tasso;
    private boolean recidiva;
    private boolean rifiutato;
    private boolean estraneo;
    private boolean incidente;

    public double getTasso() {
        return tasso;
    }

    public void setTasso(double tasso) {
        this.tasso = tasso;
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
