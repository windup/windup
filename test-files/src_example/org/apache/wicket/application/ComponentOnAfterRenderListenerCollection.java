package org.apache.wicket.application;

import org.apache.wicket.util.listener.*;
import org.apache.wicket.*;

public class ComponentOnAfterRenderListenerCollection extends ListenerCollection<IComponentOnAfterRenderListener> implements IComponentOnAfterRenderListener{
    private static final long serialVersionUID=1L;
    public void onAfterRender(final Component component){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IComponentOnAfterRenderListener>(){
            public void notify(final IComponentOnAfterRenderListener listener){
                listener.onAfterRender(component);
            }
        });
    }
}
