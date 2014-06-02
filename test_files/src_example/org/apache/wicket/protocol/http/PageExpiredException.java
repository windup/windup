package org.apache.wicket.protocol.http;

import org.apache.wicket.*;

public class PageExpiredException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    public PageExpiredException(final String message){
        super(message);
    }
    public PageExpiredException(final String message,final Exception cause){
        super(message,cause);
    }
}
