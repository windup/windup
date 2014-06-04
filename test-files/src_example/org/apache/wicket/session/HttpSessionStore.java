package org.apache.wicket.session;

import java.util.concurrent.*;
import org.apache.wicket.request.*;
import org.apache.wicket.*;
import java.io.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.protocol.http.*;
import java.util.*;
import org.slf4j.*;
import javax.servlet.http.*;

public class HttpSessionStore implements ISessionStore{
    private static Logger log;
    private final Set<UnboundListener> unboundListeners;
    private final Set<BindListener> bindListeners;
    public HttpSessionStore(){
        super();
        this.unboundListeners=(Set<UnboundListener>)new CopyOnWriteArraySet();
        this.bindListeners=(Set<BindListener>)new CopyOnWriteArraySet();
    }
    protected final HttpServletRequest getHttpServletRequest(final Request request){
        final Object containerRequest=request.getContainerRequest();
        if(containerRequest==null||!(containerRequest instanceof HttpServletRequest)){
            throw new IllegalArgumentException("Request must be ServletWebRequest");
        }
        return (HttpServletRequest)containerRequest;
    }
    final HttpSession getHttpSession(final Request request,final boolean create){
        return this.getHttpServletRequest(request).getSession(create);
    }
    public final void bind(final Request request,final Session newSession){
        if(this.getAttribute(request,"session")!=newSession){
            this.onBind(request,newSession);
            for(final BindListener listener : this.getBindListeners()){
                listener.bindingSession(request,newSession);
            }
            final HttpSession httpSession=this.getHttpSession(request,false);
            if(httpSession!=null){
                final String applicationKey=Application.get().getName();
                httpSession.setAttribute("Wicket:SessionUnbindingListener-"+applicationKey,new SessionBindingListener(applicationKey,newSession));
                this.setAttribute(request,"session",(Serializable)newSession);
            }
        }
    }
    public void flushSession(final Request request,final Session session){
        if(this.getAttribute(request,"session")!=session){
            this.bind(request,session);
        }
        else{
            this.setAttribute(request,"session",(Serializable)session);
        }
    }
    public void destroy(){
    }
    public String getSessionId(final Request request,final boolean create){
        String id=null;
        HttpSession httpSession=this.getHttpSession(request,false);
        if(httpSession!=null){
            id=httpSession.getId();
        }
        else if(create){
            httpSession=this.getHttpSession(request,true);
            id=httpSession.getId();
            final IRequestLogger logger=Application.get().getRequestLogger();
            if(logger!=null){
                logger.sessionCreated(id);
            }
        }
        return id;
    }
    public final void invalidate(final Request request){
        final HttpSession httpSession=this.getHttpSession(request,false);
        if(httpSession!=null){
            httpSession.invalidate();
        }
    }
    public final Session lookup(final Request request){
        final String sessionId=this.getSessionId(request,false);
        if(sessionId!=null){
            return (Session)this.getAttribute(request,"session");
        }
        return null;
    }
    protected void onBind(final Request request,final Session newSession){
    }
    protected void onUnbind(final String sessionId){
    }
    private String getSessionAttributePrefix(final Request request){
        String sessionAttributePrefix="wicket";
        if(request instanceof WebRequest){
            sessionAttributePrefix=WebApplication.get().getSessionAttributePrefix((WebRequest)request,null);
        }
        return sessionAttributePrefix;
    }
    public final Serializable getAttribute(final Request request,final String name){
        final HttpSession httpSession=this.getHttpSession(request,false);
        if(httpSession!=null){
            return (Serializable)httpSession.getAttribute(this.getSessionAttributePrefix(request)+name);
        }
        return null;
    }
    public final List<String> getAttributeNames(final Request request){
        final List<String> list=(List<String>)new ArrayList();
        final HttpSession httpSession=this.getHttpSession(request,false);
        if(httpSession!=null){
            final Enumeration<String> names=(Enumeration<String>)httpSession.getAttributeNames();
            final String prefix=this.getSessionAttributePrefix(request);
            while(names.hasMoreElements()){
                final String name=(String)names.nextElement();
                if(name.startsWith(prefix)){
                    list.add(name.substring(prefix.length()));
                }
            }
        }
        return list;
    }
    public final void removeAttribute(final Request request,final String name){
        final HttpSession httpSession=this.getHttpSession(request,false);
        if(httpSession!=null){
            final String attributeName=this.getSessionAttributePrefix(request)+name;
            final IRequestLogger logger=Application.get().getRequestLogger();
            if(logger!=null){
                final Object value=httpSession.getAttribute(attributeName);
                if(value!=null){
                    logger.objectRemoved(value);
                }
            }
            httpSession.removeAttribute(attributeName);
        }
    }
    public final void setAttribute(final Request request,final String name,final Serializable value){
        final HttpSession httpSession=this.getHttpSession(request,false);
        if(httpSession!=null){
            final String attributeName=this.getSessionAttributePrefix(request)+name;
            final IRequestLogger logger=Application.get().getRequestLogger();
            if(logger!=null){
                if(httpSession.getAttribute(attributeName)==null){
                    logger.objectCreated(value);
                }
                else{
                    logger.objectUpdated(value);
                }
            }
            httpSession.setAttribute(attributeName,value);
        }
    }
    public final void registerUnboundListener(final UnboundListener listener){
        this.unboundListeners.add(listener);
    }
    public final void unregisterUnboundListener(final UnboundListener listener){
        this.unboundListeners.remove(listener);
    }
    public final Set<UnboundListener> getUnboundListener(){
        return (Set<UnboundListener>)Collections.unmodifiableSet(this.unboundListeners);
    }
    public void registerBindListener(final BindListener listener){
        this.bindListeners.add(listener);
    }
    public void unregisterBindListener(final BindListener listener){
        this.bindListeners.remove(listener);
    }
    public Set<BindListener> getBindListeners(){
        return (Set<BindListener>)Collections.unmodifiableSet(this.bindListeners);
    }
    static{
        HttpSessionStore.log=LoggerFactory.getLogger(HttpSessionStore.class);
    }
    protected static final class SessionBindingListener implements HttpSessionBindingListener,Serializable{
        private static final long serialVersionUID=1L;
        private final String applicationKey;
        private final Session wicketSession;
        public SessionBindingListener(final String applicationKey,final Session wicketSession){
            super();
            this.applicationKey=applicationKey;
            this.wicketSession=wicketSession;
        }
        public void valueBound(final HttpSessionBindingEvent evg){
        }
        public void valueUnbound(final HttpSessionBindingEvent evt){
            final String sessionId=evt.getSession().getId();
            if(HttpSessionStore.log.isDebugEnabled()){
                HttpSessionStore.log.debug("Session unbound: "+sessionId);
            }
            this.wicketSession.onInvalidate();
            final Application application=Application.get(this.applicationKey);
            if(application==null){
                HttpSessionStore.log.debug("Wicket application with name '"+this.applicationKey+"' not found.");
                return;
            }
            final ISessionStore sessionStore=application.getSessionStore();
            if(sessionStore!=null){
                if(sessionStore instanceof HttpSessionStore){
                    ((HttpSessionStore)sessionStore).onUnbind(sessionId);
                }
                for(final UnboundListener listener : sessionStore.getUnboundListener()){
                    listener.sessionUnbound(sessionId);
                }
            }
        }
    }
}
