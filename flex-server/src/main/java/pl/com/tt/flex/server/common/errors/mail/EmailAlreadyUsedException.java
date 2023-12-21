package pl.com.tt.flex.server.common.errors.mail;

import lombok.Getter;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

@Getter
public class EmailAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    private final ActivityEvent activityEvent;

    public EmailAlreadyUsedException() {
        super("Email is already in use!", "userManagement", ErrorConstants.EMAIL_ALREADY_USED_TYPE);
        this.activityEvent = null;
    }

    public EmailAlreadyUsedException(String message) {
        super(message, "userManagement", ErrorConstants.EMAIL_ALREADY_USED_TYPE);
        this.activityEvent = null;
    }

    public EmailAlreadyUsedException(ActivityEvent activityEvent) {
        super("Email is already in use!", "userManagement", ErrorConstants.EMAIL_ALREADY_USED_TYPE);
        this.activityEvent = activityEvent;
    }
}
