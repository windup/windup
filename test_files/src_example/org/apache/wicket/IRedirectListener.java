package org.apache.wicket;

public interface IRedirectListener extends IRequestListener{
    public static final RequestListenerInterface INTERFACE=new RequestListenerInterface((Class<? extends IRequestListener>)IRedirectListener.class);
    void onRedirect();
}
