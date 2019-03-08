package application.authentication;

public class AuthData {
    private final String codicefiscale;
    private final String password;

    public AuthData(String codicefiscale, String password) {
        this.codicefiscale = codicefiscale;
        this.password = password;
    }

    public String getCodicefiscale() {
        return codicefiscale;
    }

    public String getPassword() {
        return password;
    }
}
