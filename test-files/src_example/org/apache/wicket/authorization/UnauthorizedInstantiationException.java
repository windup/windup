package org.apache.wicket.authorization;

import org.apache.wicket.request.component.*;
import org.apache.wicket.util.lang.*;

public class UnauthorizedInstantiationException extends AuthorizationException{
    private static final long serialVersionUID=1L;
    private final String componentClassName;
    public UnauthorizedInstantiationException(final Class<T> componentClass){
        super("Not authorized to instantiate class "+componentClass.getName());
        this.componentClassName=componentClass.getName();
    }
    public Class<? extends IRequestableComponent> getComponentClass(){
        return WicketObjects.resolveClass(this.componentClassName);
    }
}
