package org.apache.wicket.request.handler;

import org.apache.wicket.request.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.*;

public final class EmptyAjaxRequestHandler implements IRequestHandler{
    private static final int HASH=26219491;
    private static final EmptyAjaxRequestHandler instance;
    public static final EmptyAjaxRequestHandler getInstance(){
        return EmptyAjaxRequestHandler.instance;
    }
    public void respond(final IRequestCycle requestCycle){
        final WebResponse response=(WebResponse)requestCycle.getResponse();
        final String encoding=Application.get().getRequestCycleSettings().getResponseRequestEncoding();
        response.setContentType("text/xml; charset="+encoding);
        response.disableCaching();
        response.write((CharSequence)"<?xml version=\"1.0\" encoding=\"");
        response.write((CharSequence)encoding);
        response.write((CharSequence)"\"?><ajax-response></ajax-response>");
    }
    public void detach(final IRequestCycle requestCycle){
    }
    public boolean equals(final Object obj){
        return obj instanceof EmptyAjaxRequestHandler;
    }
    public int hashCode(){
        return 26219491;
    }
    public String toString(){
        return "EmptyAjaxRequestTarget";
    }
    static{
        instance=new EmptyAjaxRequestHandler();
    }
}
