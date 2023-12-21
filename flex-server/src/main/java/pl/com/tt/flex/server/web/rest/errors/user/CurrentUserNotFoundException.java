package pl.com.tt.flex.server.web.rest.errors.user;

public class CurrentUserNotFoundException extends RuntimeException {

    public CurrentUserNotFoundException(String message) {
        super(message);
    }
}
