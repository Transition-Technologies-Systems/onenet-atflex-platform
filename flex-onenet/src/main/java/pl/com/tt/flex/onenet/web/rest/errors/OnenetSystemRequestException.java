package pl.com.tt.flex.onenet.web.rest.errors;

import lombok.Getter;

@Getter
public class OnenetSystemRequestException extends RuntimeException {

	private final String msgKey;

	private final String entityName;

	private final Long objectId;

	public OnenetSystemRequestException(String message, String msgKey) {
		super(message);
		this.msgKey = msgKey;
		this.entityName = null;
		this.objectId = null;
	}

	public OnenetSystemRequestException(String message, String msgKey, String entityName) {
		super(message);
		this.msgKey = msgKey;
		this.entityName = entityName;
		this.objectId = null;
	}

	public OnenetSystemRequestException(String message, String msgKey, String entityName, Long objectId) {
		super(message);
		this.msgKey = msgKey;
		this.entityName = entityName;
		this.objectId = objectId;
	}

}
