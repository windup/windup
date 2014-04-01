package org.jboss.windup;

public class WindupServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    public WindupServiceException() {
		super();
	}

	public WindupServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public WindupServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public WindupServiceException(String message) {
		super(message);
	}

	public WindupServiceException(Throwable cause) {
		super(cause);
	}
}
