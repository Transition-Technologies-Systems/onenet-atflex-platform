package pl.com.tt.flex.server.web.rest.errors.user;

import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * This exception is thrown in case of a not activated user trying to authenticate.
 */
@Getter
public class UserNotActivatedException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;
    private final String msgKey;

    public UserNotActivatedException(String message, String msgKey) {
        super(ErrorConstants.DEFAULT_TYPE, message, Status.UNAUTHORIZED, null, null, null, getAlertParameters(msgKey));
        this.msgKey = msgKey;
    }

    private static Map<String, Object> getAlertParameters(String errorKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("message", errorKey);
        return parameters;
    }
}
