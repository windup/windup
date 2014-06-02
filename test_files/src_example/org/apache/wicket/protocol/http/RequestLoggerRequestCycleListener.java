package org.apache.wicket.protocol.http;

import org.apache.wicket.request.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.*;
import org.apache.wicket.settings.*;
import javax.servlet.http.*;
import org.apache.wicket.util.string.*;

public class RequestLoggerRequestCycleListener extends AbstractRequestCycleListener{
    private static final ThreadLocal<IRequestHandler> first;
    public void onBeginRequest(final RequestCycle cycle){
        if(!this.isRequestLoggingEnabled()){
            return;
        }
        this.registerRequestedUrl(cycle);
    }
    public void onRequestHandlerScheduled(final RequestCycle cycle,final IRequestHandler handler){
        if(!this.isRequestLoggingEnabled()){
            return;
        }
        this.registerHandler(handler);
    }
    public void onRequestHandlerResolved(final RequestCycle cycle,final IRequestHandler handler){
        if(!this.isRequestLoggingEnabled()){
            return;
        }
        this.registerHandler(handler);
    }
    public void onExceptionRequestHandlerResolved(final RequestCycle cycle,final IRequestHandler handler,final Exception exception){
        if(!this.isRequestLoggingEnabled()){
            return;
        }
        this.registerHandler(handler);
    }
    public void onEndRequest(final RequestCycle cycle){
        RequestLoggerRequestCycleListener.first.remove();
    }
    private boolean isRequestLoggingEnabled(){
        final IRequestLogger requestLogger=Application.get().getRequestLogger();
        final IRequestLoggerSettings settings=Application.get().getRequestLoggerSettings();
        return requestLogger!=null&&settings.isRequestLoggerEnabled();
    }
    private void registerRequestedUrl(final RequestCycle cycle){
        final IRequestLogger requestLogger=Application.get().getRequestLogger();
        if(cycle.getRequest().getContainerRequest() instanceof HttpServletRequest){
            final HttpServletRequest containerRequest=(HttpServletRequest)cycle.getRequest().getContainerRequest();
            final AppendingStringBuffer url=new AppendingStringBuffer((CharSequence)containerRequest.getRequestURL());
            if(containerRequest.getQueryString()!=null){
                url.append("?").append(containerRequest.getQueryString());
            }
            requestLogger.logRequestedUrl(url.toString());
        }
    }
    private void registerHandler(final IRequestHandler handler){
        final IRequestLogger requestLogger=Application.get().getRequestLogger();
        if(RequestLoggerRequestCycleListener.first.get()==null){
            RequestLoggerRequestCycleListener.first.set(handler);
            requestLogger.logEventTarget(handler);
        }
        requestLogger.logResponseTarget(handler);
    }
    static{
        first=new ThreadLocal();
    }
}
