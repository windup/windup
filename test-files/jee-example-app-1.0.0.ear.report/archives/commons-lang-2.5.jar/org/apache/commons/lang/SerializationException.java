package org.apache.commons.lang;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class SerializationException extends NestableRuntimeException{
    private static final long serialVersionUID=4029025366392702726L;
    public SerializationException(){
        super();
    }
    public SerializationException(final String msg){
        super(msg);
    }
    public SerializationException(final Throwable cause){
        super(cause);
    }
    public SerializationException(final String msg,final Throwable cause){
        super(msg,cause);
    }
}
