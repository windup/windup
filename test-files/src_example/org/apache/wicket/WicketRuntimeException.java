package org.apache.wicket;

public class WicketRuntimeException extends RuntimeException{
    private static final long serialVersionUID=1L;
    public WicketRuntimeException(){
        super();
    }
    public WicketRuntimeException(final String message){
        super(message);
    }
    public WicketRuntimeException(final String message,final Throwable cause){
        super(message,cause);
    }
    public WicketRuntimeException(final Throwable cause){
        super(cause);
    }
}
