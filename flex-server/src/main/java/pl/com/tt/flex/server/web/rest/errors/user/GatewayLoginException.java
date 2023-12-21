package pl.com.tt.flex.server.web.rest.errors.user;

/**
 * Niepozadane bledy dotyczace logowania sie uzytkownika do danego modulu systemu
 */
public class GatewayLoginException extends RuntimeException {

    public GatewayLoginException(String message) {
        super(message);
    }
}
