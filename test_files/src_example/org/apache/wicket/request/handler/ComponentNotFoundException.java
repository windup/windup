package org.apache.wicket.request.handler;

import org.apache.wicket.*;

public class ComponentNotFoundException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    public ComponentNotFoundException(){
        super();
    }
    public ComponentNotFoundException(final String message){
        super(message);
    }
    public ComponentNotFoundException(final String message,final Throwable cause){
        super(message,cause);
    }
    public ComponentNotFoundException(final Throwable cause){
        super(cause);
    }
}
