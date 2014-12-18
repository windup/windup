package org.apache.commons.lang;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class UnhandledException extends NestableRuntimeException{
    private static final long serialVersionUID=1832101364842773720L;
    public UnhandledException(final Throwable cause){
        super(cause);
    }
    public UnhandledException(final String message,final Throwable cause){
        super(message,cause);
    }
}
