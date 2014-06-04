package org.apache.wicket.authorization;

import org.apache.wicket.*;

public class UnauthorizedActionException extends AuthorizationException{
    private static final long serialVersionUID=1L;
    private final transient Action action;
    private final transient Component component;
    public UnauthorizedActionException(final Component component,final Action action){
        super("Component "+component+" does not permit action "+action);
        this.component=component;
        this.action=action;
    }
    public Action getAction(){
        return this.action;
    }
    public Component getComponent(){
        return this.component;
    }
}
