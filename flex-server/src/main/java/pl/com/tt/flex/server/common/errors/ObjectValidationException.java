package pl.com.tt.flex.server.common.errors;

import java.util.List;

import lombok.Getter;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;

@Getter
public class ObjectValidationException extends RuntimeException {

    private final String msgKey;

    private final String entityName;

    private final ActivityEvent activityEvent;

    private final Long objectId;

    private final List<Object> collection;

    public ObjectValidationException(String message, String msgKey) {
        super(message);
        this.msgKey = msgKey;
        this.entityName = null;
        this.activityEvent = null;
        this.objectId = null;
        this.collection = null;
    }

    public ObjectValidationException(String message, String msgKey, String entityName) {
        super(message);
        this.msgKey = msgKey;
        this.entityName = entityName;
        this.activityEvent = null;
        this.objectId = null;
        this.collection = null;
    }

    public ObjectValidationException(String message, String msgKey, String entityName, ActivityEvent activityEvent, Long objectId) {
        super(message);
        this.msgKey = msgKey;
        this.entityName = entityName;
        this.activityEvent = activityEvent;
        this.objectId = objectId;
        this.collection = null;
    }

    public ObjectValidationException(String message, String msgKey, String entityName, List<Object> collection) {
        super(message);
        this.msgKey = msgKey;
        this.entityName = entityName;
        this.activityEvent = getActivityEvent();
        this.collection = collection;
        this.objectId = null;
    }
}
