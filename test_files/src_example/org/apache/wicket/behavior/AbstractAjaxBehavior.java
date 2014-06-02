package org.apache.wicket.behavior;

import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public abstract class AbstractAjaxBehavior extends Behavior implements IBehaviorListener{
    private static final long serialVersionUID=1L;
    private Component component;
    public final void bind(final Component hostComponent){
        if(hostComponent==null){
            throw new IllegalArgumentException("Argument hostComponent must be not null");
        }
        if(this.component!=null){
            throw new IllegalStateException("this kind of handler cannot be attached to multiple components; it is already attached to component "+this.component+", but component "+hostComponent+" wants to be attached too");
        }
        this.component=hostComponent;
        this.onBind();
    }
    public CharSequence getCallbackUrl(){
        if(this.getComponent()==null){
            throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
        }
        final RequestListenerInterface rli=IBehaviorListener.INTERFACE;
        return this.getComponent().urlFor(this,rli,new PageParameters());
    }
    public final void onComponentTag(final Component component,final ComponentTag tag){
        this.onComponentTag(tag);
    }
    public final void afterRender(final Component hostComponent){
        this.onComponentRendered();
    }
    protected final Component getComponent(){
        return this.component;
    }
    protected void onComponentTag(final ComponentTag tag){
    }
    protected void onBind(){
    }
    protected void onComponentRendered(){
    }
    public boolean getStatelessHint(final Component component){
        return false;
    }
    public final void unbind(Component component){
        this.onUnbind();
        component=null;
        super.unbind(component);
    }
    protected void onUnbind(){
    }
}
