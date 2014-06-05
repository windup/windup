package org.apache.wicket.application;

import org.apache.wicket.util.listener.*;
import org.apache.wicket.*;

public class ComponentInitializationListenerCollection extends ListenerCollection<IComponentInitializationListener> implements IComponentInitializationListener{
    private static final long serialVersionUID=1L;
    public void onInitialize(final Component component){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IComponentInitializationListener>(){
            public void notify(final IComponentInitializationListener listener){
                listener.onInitialize(component);
            }
        });
    }
}
