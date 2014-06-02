package org.apache.wicket.request.mapper;

import org.apache.wicket.request.cycle.*;
import org.apache.wicket.session.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.handler.*;

public class BufferedResponseMapper implements IRequestMapper{
    protected String getSessionId(){
        String sessionId=null;
        if(Application.exists()&&RequestCycle.get()!=null){
            final ISessionStore sessionStore=Application.get().getSessionStore();
            final IRequestCycle requestCycle=(IRequestCycle)RequestCycle.get();
            final Session session=sessionStore.lookup(requestCycle.getRequest());
            if(session!=null){
                sessionId=session.getId();
            }
        }
        return sessionId;
    }
    protected boolean hasBufferedResponse(final Url url){
        final String sessionId=this.getSessionId();
        boolean hasResponse=false;
        if(!Strings.isEmpty((CharSequence)sessionId)){
            hasResponse=WebApplication.get().hasBufferedResponse(sessionId,url);
        }
        return hasResponse;
    }
    protected BufferedWebResponse getAndRemoveBufferedResponse(final Url url){
        final String sessionId=this.getSessionId();
        BufferedWebResponse response=null;
        if(!Strings.isEmpty((CharSequence)sessionId)){
            response=WebApplication.get().getAndRemoveBufferedResponse(sessionId,url);
        }
        return response;
    }
    private Request getRequest(final Request original){
        if(RequestCycle.get()!=null){
            return RequestCycle.get().getRequest();
        }
        return original;
    }
    public IRequestHandler mapRequest(Request request){
        request=this.getRequest(request);
        final BufferedWebResponse response=this.getAndRemoveBufferedResponse(request.getUrl());
        if(response!=null){
            return (IRequestHandler)new BufferedResponseRequestHandler(response);
        }
        return null;
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        return null;
    }
    public int getCompatibilityScore(Request request){
        request=this.getRequest(request);
        if(this.hasBufferedResponse(request.getUrl())){
            return Integer.MAX_VALUE;
        }
        return 0;
    }
}
