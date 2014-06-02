package org.apache.wicket;

import org.apache.wicket.util.listener.*;

public class ApplicationListenerCollection extends ListenerCollection<IApplicationListener> implements IApplicationListener{
    public void onAfterInitialized(final Application application){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IApplicationListener>(){
            public void notify(final IApplicationListener listener){
                listener.onAfterInitialized(application);
            }
        });
    }
    public void onBeforeDestroyed(final Application application){
        this.reversedNotifyIgnoringExceptions((ListenerCollection.INotifier)new ListenerCollection.INotifier<IApplicationListener>(){
            public void notify(final IApplicationListener listener){
                listener.onBeforeDestroyed(application);
            }
        });
    }
}
