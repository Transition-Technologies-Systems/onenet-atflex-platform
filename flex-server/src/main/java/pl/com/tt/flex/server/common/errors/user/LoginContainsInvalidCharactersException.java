package pl.com.tt.flex.server.common.errors.user;

import lombok.Getter;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

@Getter
public class LoginContainsInvalidCharactersException extends BadRequestAlertException {
    private static final long serialVersionUID = 1L;

    private final ActivityEvent activityEvent;

    public LoginContainsInvalidCharactersException() {
        super("String contains invalid characters", "userManagement", ErrorConstants.LOGIN_CONTAINS_INVALID_CHARACTERS);
        this.activityEvent = null;
    }

    public LoginContainsInvalidCharactersException(String message) {
        super(message, "userManagement", ErrorConstants.LOGIN_CONTAINS_INVALID_CHARACTERS);
        this.activityEvent = null;
    }

    public LoginContainsInvalidCharactersException(ActivityEvent activityEvent) {
        super("String contains invalid characters", "userManagement", ErrorConstants.LOGIN_CONTAINS_INVALID_CHARACTERS);
        this.activityEvent = activityEvent;
    }
}
