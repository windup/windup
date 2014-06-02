package org.apache.wicket.page;

import org.apache.wicket.*;
import org.apache.wicket.request.cycle.*;
import java.io.*;

public class DefaultPageManagerContext implements IPageManagerContext{
    private final MetaDataKey<Object> requestCycleMetaDataKey;
    public DefaultPageManagerContext(){
        super();
        this.requestCycleMetaDataKey=new MetaDataKey<Object>(){
            private static final long serialVersionUID=1L;
        };
    }
    public void bind(){
        Session.get().bind();
    }
    public Object getRequestData(){
        final RequestCycle requestCycle=RequestCycle.get();
        if(requestCycle==null){
            throw new IllegalStateException("Not a request thread.");
        }
        return requestCycle.getMetaData(this.requestCycleMetaDataKey);
    }
    public Serializable getSessionAttribute(final String key){
        return Session.get().getAttribute(key);
    }
    public String getSessionId(){
        return Session.get().getId();
    }
    public void setRequestData(final Object data){
        final RequestCycle requestCycle=RequestCycle.get();
        if(requestCycle==null){
            throw new IllegalStateException("Not a request thread.");
        }
        requestCycle.setMetaData(this.requestCycleMetaDataKey,data);
    }
    public void setSessionAttribute(final String key,final Serializable value){
        Session.get().setAttribute(key,value);
    }
}
