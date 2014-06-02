package org.apache.wicket.authorization;

import org.apache.wicket.*;

public abstract class AuthorizationException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    public AuthorizationException(){
        super();
    }
    public AuthorizationException(final String message){
        super(message);
    }
    public AuthorizationException(final String message,final Throwable cause){
        super(message,cause);
    }
    public AuthorizationException(final Throwable cause){
        super(cause);
    }
}
