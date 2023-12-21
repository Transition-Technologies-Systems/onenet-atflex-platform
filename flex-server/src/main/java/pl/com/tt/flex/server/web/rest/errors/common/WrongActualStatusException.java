package pl.com.tt.flex.server.web.rest.errors.common;

import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

/**
 * Common error to throw when actual status field of entity should be different to perform operation
 */
public class WrongActualStatusException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public WrongActualStatusException(String expectedStatus, String actualStatus) {
        super("Wrong actual status. Expected: " + expectedStatus + ", actual: " + actualStatus, "status", ErrorConstants.WRONG_ACTUAL_STATUS);
    }
}
