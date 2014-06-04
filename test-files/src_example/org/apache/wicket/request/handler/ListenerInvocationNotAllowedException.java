package org.apache.wicket.request.handler;

import org.apache.wicket.behavior.*;
import org.apache.wicket.*;

public class ListenerInvocationNotAllowedException extends RuntimeException{
    private static final long serialVersionUID=1L;
    private final Component component;
    private final Behavior behavior;
    private final RequestListenerInterface iface;
    public ListenerInvocationNotAllowedException(final RequestListenerInterface iface,final Component component,final Behavior behavior,final String message){
        super(message+detail(iface,component,behavior));
        this.iface=iface;
        this.component=component;
        this.behavior=behavior;
    }
    private static String detail(final RequestListenerInterface iface,final Component component,final Behavior behavior){
        final StringBuilder detail=new StringBuilder("Component: ").append(component.toString(false));
        if(behavior!=null){
            detail.append(" Behavior: ").append(behavior.toString());
        }
        detail.append(" Listener: ").append(iface.toString());
        return detail.toString();
    }
    public Component getComponent(){
        return this.component;
    }
    public Behavior getBehavior(){
        return this.behavior;
    }
}
