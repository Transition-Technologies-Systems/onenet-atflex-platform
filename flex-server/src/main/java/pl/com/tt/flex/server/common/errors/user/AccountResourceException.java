package pl.com.tt.flex.server.common.errors.user;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class AccountResourceException extends AbstractThrowableProblem {
    public AccountResourceException(String message) {
        super(null, message, Status.BAD_REQUEST);
    }
}
