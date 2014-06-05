package org.apache.wicket.protocol.http.mock;

import java.io.*;
import org.apache.wicket.util.value.*;
import javax.servlet.*;
import java.util.*;
import javax.servlet.http.*;

public class MockHttpSession implements HttpSession,Serializable{
    private static final long serialVersionUID=1L;
    private final ValueMap attributes;
    private final transient ServletContext context;
    private final long creationTime;
    private final String id;
    private long lastAccessedTime;
    private boolean temporary;
    public MockHttpSession(final ServletContext context){
        super();
        this.attributes=new ValueMap();
        this.creationTime=System.currentTimeMillis();
        this.id=UUID.randomUUID().toString().replace(':','_').replace('-','_');
        this.lastAccessedTime=0L;
        this.temporary=true;
        this.context=context;
    }
    public Object getAttribute(final String name){
        return this.attributes.get((Object)name);
    }
    public Enumeration<String> getAttributeNames(){
        return (Enumeration<String>)Collections.enumeration(this.attributes.keySet());
    }
    public long getCreationTime(){
        return this.creationTime;
    }
    public String getId(){
        return this.id;
    }
    public long getLastAccessedTime(){
        return this.lastAccessedTime;
    }
    public int getMaxInactiveInterval(){
        return 0;
    }
    public ServletContext getServletContext(){
        return this.context;
    }
    @Deprecated
    public HttpSessionContext getSessionContext(){
        return null;
    }
    @Deprecated
    public Object getValue(final String name){
        return this.getAttribute(name);
    }
    @Deprecated
    public String[] getValueNames(){
        final String[] result=new String[this.attributes.size()];
        return (String[])this.attributes.keySet().toArray(result);
    }
    public void invalidate(){
        this.attributes.clear();
    }
    public boolean isNew(){
        return false;
    }
    @Deprecated
    public void putValue(final String name,final Object o){
        this.setAttribute(name,o);
    }
    public void removeAttribute(final String name){
        this.attributes.remove((Object)name);
    }
    @Deprecated
    public void removeValue(final String name){
        this.removeAttribute(name);
    }
    public void setAttribute(final String name,final Object o){
        this.attributes.put(name,o);
    }
    public void setMaxInactiveInterval(final int i){
    }
    public void timestamp(){
        this.lastAccessedTime=System.currentTimeMillis();
    }
    public final boolean isTemporary(){
        return this.temporary;
    }
    public final void setTemporary(final boolean temporary){
        this.temporary=temporary;
    }
}
