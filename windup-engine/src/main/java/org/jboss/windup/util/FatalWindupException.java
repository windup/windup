package org.jboss.windup.util;

public class FatalWindupException extends RuntimeException {

	private static final long serialVersionUID = 8801617347284180568L;
	
	public FatalWindupException(String message) {
		super(message);
	}
	
	public FatalWindupException(Throwable t) {
		super(t);
	}
	
	public FatalWindupException(String message, Throwable t) {
		super(message, t);
	}
}
