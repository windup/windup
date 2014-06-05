package org.apache.wicket.markup;

import org.apache.wicket.*;

public final class MarkupNotFoundException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    public MarkupNotFoundException(final String message){
        super(message);
    }
    public MarkupNotFoundException(final String message,final Throwable cause){
        super(message,cause);
    }
}
