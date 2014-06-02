package org.apache.wicket.behavior;

import org.apache.wicket.*;

public interface IBehaviorListener extends IRequestListener{
    public static final RequestListenerInterface INTERFACE=new RequestListenerInterface((Class<? extends IRequestListener>)IBehaviorListener.class);
    void onRequest();
}
