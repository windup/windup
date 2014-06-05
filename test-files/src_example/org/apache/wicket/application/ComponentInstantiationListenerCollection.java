package org.apache.wicket.application;

import org.apache.wicket.util.listener.*;
import org.apache.wicket.*;

public class ComponentInstantiationListenerCollection extends ListenerCollection<IComponentInstantiationListener> implements IComponentInstantiationListener{
    private static final long serialVersionUID=1L;
    public void onInstantiation(final Component component){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IComponentInstantiationListener>(){
            public void notify(final IComponentInstantiationListener listener){
                listener.onInstantiation(component);
            }
        });
    }
}
