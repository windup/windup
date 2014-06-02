package org.apache.wicket;

import org.apache.wicket.util.listener.*;
import org.apache.wicket.behavior.*;

public class BehaviorInstantiationListenerCollection extends ListenerCollection<IBehaviorInstantiationListener> implements IBehaviorInstantiationListener{
    public void onInstantiation(final Behavior behavior){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IBehaviorInstantiationListener>(){
            public void notify(final IBehaviorInstantiationListener listener){
                listener.onInstantiation(behavior);
            }
        });
    }
}
