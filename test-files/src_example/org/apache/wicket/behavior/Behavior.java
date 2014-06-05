package org.apache.wicket.behavior;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import java.lang.reflect.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.event.*;

public abstract class Behavior implements IClusterable,IComponentAwareEventSink,IComponentAwareHeaderContributor{
    private static final long serialVersionUID=1L;
    public Behavior(){
        super();
        if(Application.exists()){
            Application.get().getBehaviorInstantiationListeners().onInstantiation(this);
        }
    }
    public void beforeRender(final Component component){
    }
    public void afterRender(final Component component){
    }
    public void bind(final Component component){
    }
    public void unbind(final Component component){
    }
    public void detach(final Component component){
    }
    public void onException(final Component component,final RuntimeException exception){
    }
    public boolean getStatelessHint(final Component component){
        return !(this instanceof IBehaviorListener);
    }
    public boolean isEnabled(final Component component){
        return true;
    }
    public void onComponentTag(final Component component,final ComponentTag tag){
    }
    public boolean isTemporary(final Component component){
        return false;
    }
    @Deprecated
    public boolean canCallListenerInterface(final Component component){
        return this.isEnabled(component)&&component.canCallListenerInterface();
    }
    public boolean canCallListenerInterface(final Component component,final Method method){
        return this.canCallListenerInterface(component)&&this.isEnabled(component)&&component.canCallListenerInterface(method);
    }
    public void renderHead(final Component component,final IHeaderResponse response){
    }
    public void onConfigure(final Component component){
    }
    public void onEvent(final Component component,final IEvent<?> event){
    }
}
