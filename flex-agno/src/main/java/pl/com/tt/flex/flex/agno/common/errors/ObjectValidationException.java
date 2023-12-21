package pl.com.tt.flex.flex.agno.common.errors;

import lombok.Getter;

@Getter
public class ObjectValidationException extends Exception {

	private final String msgKey;

	private final String entityName;

	private final Long objectId;

	public ObjectValidationException(String message, String msgKey) {
		super(message);
		this.msgKey = msgKey;
		this.entityName = null;
		this.objectId = null;
	}

	public ObjectValidationException(String message, String msgKey, String entityName) {
		super(message);
		this.msgKey = msgKey;
		this.entityName = entityName;
		this.objectId = null;
	}

	public ObjectValidationException(String message, String msgKey, String entityName, Long objectId) {
		super(message);
		this.msgKey = msgKey;
		this.entityName = entityName;
		this.objectId = objectId;
	}

}
