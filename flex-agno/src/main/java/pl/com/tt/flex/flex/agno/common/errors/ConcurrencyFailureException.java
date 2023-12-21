package pl.com.tt.flex.flex.agno.common.errors;

import lombok.Getter;

@Getter
public class ConcurrencyFailureException extends org.springframework.dao.ConcurrencyFailureException {

	private final String msgKey;

	private final Long objectId;


	public ConcurrencyFailureException(String msg) {
		super(msg);
		this.msgKey = null;
		this.objectId = null;
	}

	public ConcurrencyFailureException(String msg, Throwable cause) {
		super(msg, cause);
		this.msgKey = null;
		this.objectId = null;
	}

	public ConcurrencyFailureException(String msg, String msgKey, long objectId) {
		super(msg);
		this.msgKey = msgKey;
		this.objectId = objectId;
	}
}
