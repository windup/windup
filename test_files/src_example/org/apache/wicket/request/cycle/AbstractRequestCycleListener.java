package org.apache.wicket.request.cycle;

import org.apache.wicket.request.*;

public abstract class AbstractRequestCycleListener implements IRequestCycleListener{
    public void onBeginRequest(final RequestCycle cycle){
    }
    public void onEndRequest(final RequestCycle cycle){
    }
    public void onDetach(final RequestCycle cycle){
    }
    public void onRequestHandlerScheduled(final RequestCycle cycle,final IRequestHandler handler){
    }
    public void onRequestHandlerResolved(final RequestCycle cycle,final IRequestHandler handler){
    }
    public IRequestHandler onException(final RequestCycle cycle,final Exception ex){
        return null;
    }
    public void onExceptionRequestHandlerResolved(final RequestCycle cycle,final IRequestHandler handler,final Exception exception){
    }
    public void onRequestHandlerExecuted(final RequestCycle cycle,final IRequestHandler handler){
    }
    public void onUrlMapped(final RequestCycle cycle,final IRequestHandler handler,final Url url){
    }
}
