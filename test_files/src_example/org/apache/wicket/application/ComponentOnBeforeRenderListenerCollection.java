package org.apache.wicket.application;

import org.apache.wicket.util.listener.*;
import org.apache.wicket.*;

public class ComponentOnBeforeRenderListenerCollection extends ListenerCollection<IComponentOnBeforeRenderListener> implements IComponentOnBeforeRenderListener{
    private static final long serialVersionUID=1L;
    public void onBeforeRender(final Component component){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IComponentOnBeforeRenderListener>(){
            public void notify(final IComponentOnBeforeRenderListener listener){
                listener.onBeforeRender(component);
            }
        });
    }
}
