package pl.com.tt.flex.server.common.errors;

import lombok.Getter;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;

@Getter
public class ConcurrencyFailureException extends org.springframework.dao.ConcurrencyFailureException {

    private final String msgKey;

    private final Long objectId;

    private final ActivityEvent activityEvent;

    public ConcurrencyFailureException(String msg) {
        super(msg);
        this.msgKey = null;
        this.objectId = null;
        this.activityEvent = null;
    }

    public ConcurrencyFailureException(String msg, Throwable cause) {
        super(msg, cause);
        this.msgKey = null;
        this.objectId = null;
        this.activityEvent = null;
    }

    public ConcurrencyFailureException(String msg, String msgKey, ActivityEvent activityEvent, long objectId) {
        super(msg);
        this.msgKey = msgKey;
        this.objectId = objectId;
        this.activityEvent = activityEvent;
    }
}
