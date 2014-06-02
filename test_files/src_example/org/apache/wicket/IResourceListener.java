package org.apache.wicket;

public interface IResourceListener extends IRequestListener{
    public static final RequestListenerInterface INTERFACE=new RequestListenerInterface((Class<? extends IRequestListener>)IResourceListener.class).setIncludeRenderCount(false).setRenderPageAfterInvocation(false);
    void onResourceRequested();
}
