package pl.com.tt.flex.server.web.rest.errors.user;

import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

public class LoginAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public LoginAlreadyUsedException() {
        super("Login name already used!", "userManagement", ErrorConstants.LOGIN_ALREADY_USED_TYPE);
    }
}
