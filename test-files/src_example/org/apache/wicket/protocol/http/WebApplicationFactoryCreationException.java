package org.apache.wicket.protocol.http;

import org.apache.wicket.*;

public class WebApplicationFactoryCreationException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    public WebApplicationFactoryCreationException(final String appFactoryClassName,final Exception e){
        super("Unable to create application factory of class "+appFactoryClassName,e);
    }
}
