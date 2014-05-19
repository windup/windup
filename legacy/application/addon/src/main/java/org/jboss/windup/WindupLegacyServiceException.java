package org.jboss.windup;

public class WindupLegacyServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    public WindupLegacyServiceException() {
		super();
	}

	public WindupLegacyServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public WindupLegacyServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public WindupLegacyServiceException(String message) {
		super(message);
	}

	public WindupLegacyServiceException(Throwable cause) {
		super(cause);
	}
}
