package org.apache.wicket.request.cycle;

import org.apache.wicket.request.*;

public interface IRequestCycleListener{
    void onBeginRequest(RequestCycle p0);
    void onEndRequest(RequestCycle p0);
    void onDetach(RequestCycle p0);
    void onRequestHandlerResolved(RequestCycle p0,IRequestHandler p1);
    void onRequestHandlerScheduled(RequestCycle p0,IRequestHandler p1);
    IRequestHandler onException(RequestCycle p0,Exception p1);
    void onExceptionRequestHandlerResolved(RequestCycle p0,IRequestHandler p1,Exception p2);
    void onRequestHandlerExecuted(RequestCycle p0,IRequestHandler p1);
    void onUrlMapped(RequestCycle p0,IRequestHandler p1,Url p2);
}
