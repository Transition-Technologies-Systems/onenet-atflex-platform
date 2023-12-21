package pl.com.tt.flex.server.web.rest.errors.user;

import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

public class NoPermissionToLoginException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public NoPermissionToLoginException(String userName, String appName) {
        super("User " + userName + " has no permission to login into system: " + appName, "userManagement", ErrorConstants.NO_PERMISSION_TO_LOGIN);
    }
}
