package org.apache.wicket.mock;

import org.apache.wicket.session.*;
import java.io.*;
import org.apache.wicket.*;
import java.util.concurrent.*;
import org.apache.wicket.request.*;
import java.util.*;

public class MockSessionStore implements ISessionStore{
    private String sessionId;
    private final Map<String,Serializable> attributes;
    private Session session;
    private final Set<UnboundListener> unboundListeners;
    public MockSessionStore(){
        super();
        this.attributes=(Map<String,Serializable>)new HashMap();
        this.unboundListeners=(Set<UnboundListener>)new CopyOnWriteArraySet();
    }
    public void bind(final Request request,final Session newSession){
        this.session=newSession;
    }
    public void destroy(){
        this.cleanup();
    }
    public Serializable getAttribute(final Request request,final String name){
        return (Serializable)this.attributes.get(name);
    }
    public List<String> getAttributeNames(final Request request){
        return (List<String>)Collections.unmodifiableList(new ArrayList(this.attributes.keySet()));
    }
    public String getSessionId(final Request request,final boolean create){
        if(create&&this.sessionId==null){
            this.sessionId=UUID.randomUUID().toString();
        }
        return this.sessionId;
    }
    private void cleanup(){
        this.sessionId=null;
        this.attributes.clear();
        this.session=null;
    }
    public void invalidate(final Request request){
        final String sessId=this.sessionId;
        this.cleanup();
        for(final UnboundListener l : this.unboundListeners){
            l.sessionUnbound(sessId);
        }
    }
    public Session lookup(final Request request){
        return this.session;
    }
    public void registerUnboundListener(final UnboundListener listener){
        this.unboundListeners.add(listener);
    }
    public void removeAttribute(final Request request,final String name){
        this.attributes.remove(name);
    }
    public final Set<UnboundListener> getUnboundListener(){
        return (Set<UnboundListener>)Collections.unmodifiableSet(this.unboundListeners);
    }
    public void setAttribute(final Request request,final String name,final Serializable value){
        this.attributes.put(name,value);
    }
    public void unregisterUnboundListener(final UnboundListener listener){
        this.unboundListeners.remove(listener);
    }
    public void flushSession(final Request request,final Session session){
        this.session=session;
    }
}
