package org.apache.wicket;

import org.apache.wicket.util.listener.*;

public class SessionListenerCollection extends ListenerCollection<ISessionListener> implements ISessionListener{
    public void onCreated(final Session session){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<ISessionListener>(){
            public void notify(final ISessionListener listener){
                listener.onCreated(session);
            }
        });
    }
}
