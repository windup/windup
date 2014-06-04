package org.apache.wicket.request.cycle;

import org.apache.wicket.request.*;

public final class RequestCycleContext{
    private Request request;
    private Response response;
    private IRequestMapper requestMapper;
    private IExceptionMapper exceptionMapper;
    public RequestCycleContext(final Request request,final Response response,final IRequestMapper requestMapper,final IExceptionMapper exceptionMapper){
        super();
        this.request=request;
        this.response=response;
        this.requestMapper=requestMapper;
        this.exceptionMapper=exceptionMapper;
    }
    public Request getRequest(){
        return this.request;
    }
    public Response getResponse(){
        return this.response;
    }
    public IRequestMapper getRequestMapper(){
        return this.requestMapper;
    }
    public IExceptionMapper getExceptionMapper(){
        return this.exceptionMapper;
    }
    public void setRequest(final Request request){
        this.request=request;
    }
    public void setResponse(final Response response){
        this.response=response;
    }
    public void setRequestMapper(final IRequestMapper requestMapper){
        this.requestMapper=requestMapper;
    }
    public void setExceptionMapper(final IExceptionMapper exceptionMapper){
        this.exceptionMapper=exceptionMapper;
    }
}
