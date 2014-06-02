package org.apache.wicket;

import org.apache.wicket.feedback.*;
import org.apache.wicket.session.*;
import java.io.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.*;
import org.apache.wicket.authorization.*;
import org.apache.wicket.application.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.page.*;
import org.apache.wicket.event.*;
import org.slf4j.*;
import org.apache.wicket.util.*;
import org.apache.wicket.util.time.*;

public abstract class Session implements IClusterable,IEventSink{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    public static final String SESSION_ATTRIBUTE_NAME="session";
    private int sequence;
    private int pageId;
    private final IProvider<PageAccessSynchronizer> pageAccessSynchronizer;
    protected ClientInfo clientInfo;
    private transient boolean dirty;
    private final FeedbackMessages feedbackMessages;
    private String id;
    private Locale locale;
    private MetaDataEntry<?>[] metaData;
    private transient boolean sessionInvalidated;
    private transient ISessionStore sessionStore;
    private String style;
    private transient Map<String,Serializable> temporarySessionAttributes;
    public static boolean exists(){
        return ThreadContext.getSession()!=null;
    }
    public static Session get(){
        final Session session=ThreadContext.getSession();
        if(session!=null){
            return session;
        }
        return Application.get().fetchCreateAndSetSession(RequestCycle.get());
    }
    public Session(final Request request){
        super();
        this.sequence=1;
        this.pageId=0;
        this.dirty=false;
        this.feedbackMessages=new FeedbackMessages();
        this.id=null;
        this.sessionInvalidated=false;
        this.locale=request.getLocale();
        if(this.locale==null){
            throw new IllegalStateException("Request#getLocale() cannot return null, request has to have a locale set on it");
        }
        this.pageAccessSynchronizer=(IProvider<PageAccessSynchronizer>)new PageAccessSynchronizerProvider();
    }
    public final void bind(){
        if(RequestCycle.get()==null){
            return;
        }
        final ISessionStore store=this.getSessionStore();
        final Request request=RequestCycle.get().getRequest();
        if(store.lookup(request)==null){
            this.id=store.getSessionId(request,true);
            store.bind(request,this);
            if(this.temporarySessionAttributes!=null){
                for(final Map.Entry<String,Serializable> entry : this.temporarySessionAttributes.entrySet()){
                    store.setAttribute(request,String.valueOf(entry.getKey()),(Serializable)entry.getValue());
                }
                this.temporarySessionAttributes=null;
            }
        }
    }
    public abstract void cleanupFeedbackMessages();
    public final void clear(){
        if(!this.isTemporary()){
            this.getPageManager().sessionExpired(this.getId());
        }
    }
    public final void error(final Serializable message){
        this.addFeedbackMessage(message,400);
    }
    public final void fatal(final Serializable message){
        this.addFeedbackMessage(message,500);
    }
    public final void debug(final Serializable message){
        this.addFeedbackMessage(message,100);
    }
    public final Application getApplication(){
        return Application.get();
    }
    public IAuthorizationStrategy getAuthorizationStrategy(){
        return this.getApplication().getSecuritySettings().getAuthorizationStrategy();
    }
    public final IClassResolver getClassResolver(){
        return this.getApplication().getApplicationSettings().getClassResolver();
    }
    public abstract ClientInfo getClientInfo();
    public final FeedbackMessages getFeedbackMessages(){
        return this.feedbackMessages;
    }
    public final String getId(){
        if(this.id==null){
            final RequestCycle requestCycle=RequestCycle.get();
            if(requestCycle!=null){
                this.id=this.getSessionStore().getSessionId(requestCycle.getRequest(),false);
            }
            if(this.id!=null){
                this.dirty();
            }
        }
        return this.id;
    }
    public Locale getLocale(){
        return this.locale;
    }
    public final synchronized <M extends Serializable> M getMetaData(final MetaDataKey<M> key){
        return key.get(this.metaData);
    }
    protected boolean isCurrentRequestValid(final RequestCycle lockedRequestCycle){
        return true;
    }
    public IPageFactory getPageFactory(){
        return this.getApplication().getPageFactory();
    }
    public final long getSizeInBytes(){
        return WicketObjects.sizeof((Serializable)this);
    }
    public final String getStyle(){
        return this.style;
    }
    public final void info(final Serializable message){
        this.addFeedbackMessage(message,200);
    }
    public final void success(final Serializable message){
        this.addFeedbackMessage(message,250);
    }
    public void invalidate(){
        this.sessionInvalidated=true;
    }
    private void destroy(){
        if(this.sessionStore!=null){
            this.sessionStore.invalidate(RequestCycle.get().getRequest());
            this.sessionStore=null;
        }
    }
    public void invalidateNow(){
        this.invalidate();
        this.destroy();
    }
    public void replaceSession(){
        this.destroy();
        this.bind();
    }
    public final boolean isSessionInvalidated(){
        return this.sessionInvalidated;
    }
    public final boolean isTemporary(){
        return this.getId()==null;
    }
    public final void setClientInfo(final ClientInfo clientInfo){
        this.clientInfo=clientInfo;
        this.dirty();
    }
    public void setLocale(final Locale locale){
        if(locale==null){
            throw new IllegalArgumentException("Argument 'locale' must not be null");
        }
        if(!Objects.equal((Object)this.locale,(Object)locale)){
            this.dirty();
        }
        this.locale=locale;
    }
    public final synchronized void setMetaData(final MetaDataKey<?> key,final Serializable object){
        this.metaData=key.set(this.metaData,object);
        this.dirty();
    }
    public final Session setStyle(final String style){
        this.style=style;
        this.dirty();
        return this;
    }
    public final void warn(final Serializable message){
        this.addFeedbackMessage(message,300);
    }
    private void addFeedbackMessage(final Serializable message,final int level){
        this.getFeedbackMessages().add(null,message,level);
        this.dirty();
    }
    public void detach(){
        if(this.sessionInvalidated){
            this.invalidateNow();
        }
    }
    public void internalDetach(){
        if(this.dirty){
            final Request request=RequestCycle.get().getRequest();
            this.getSessionStore().flushSession(request,this);
        }
        this.dirty=false;
    }
    public final void dirty(){
        this.dirty=true;
    }
    public final Serializable getAttribute(final String name){
        if(!this.isTemporary()){
            final RequestCycle cycle=RequestCycle.get();
            if(cycle!=null){
                return this.getSessionStore().getAttribute(cycle.getRequest(),name);
            }
        }
        else if(this.temporarySessionAttributes!=null){
            return (Serializable)this.temporarySessionAttributes.get(name);
        }
        return null;
    }
    protected final List<String> getAttributeNames(){
        if(!this.isTemporary()){
            final RequestCycle cycle=RequestCycle.get();
            if(cycle!=null){
                return (List<String>)Collections.unmodifiableList(this.getSessionStore().getAttributeNames(cycle.getRequest()));
            }
        }
        else if(this.temporarySessionAttributes!=null){
            return (List<String>)Collections.unmodifiableList(new ArrayList(this.temporarySessionAttributes.keySet()));
        }
        return (List<String>)Collections.emptyList();
    }
    protected ISessionStore getSessionStore(){
        if(this.sessionStore==null){
            this.sessionStore=this.getApplication().getSessionStore();
        }
        return this.sessionStore;
    }
    protected final void removeAttribute(final String name){
        if(!this.isTemporary()){
            final RequestCycle cycle=RequestCycle.get();
            if(cycle!=null){
                this.getSessionStore().removeAttribute(cycle.getRequest(),name);
            }
        }
        else if(this.temporarySessionAttributes!=null){
            this.temporarySessionAttributes.remove(name);
        }
    }
    public final void setAttribute(final String name,final Serializable value){
        if(!this.isTemporary()){
            final RequestCycle cycle=RequestCycle.get();
            if(cycle==null){
                throw new IllegalStateException("Cannot set the attribute: no RequestCycle available.  If you get this error when using WicketTester.startPage(Page), make sure to call WicketTester.createRequestCycle() beforehand.");
            }
            final ISessionStore store=this.getSessionStore();
            final Request request=cycle.getRequest();
            if(value==this){
                final Object current=store.getAttribute(request,name);
                if(current==null){
                    final String id=store.getSessionId(request,false);
                    if(id!=null){
                        store.bind(request,(Session)value);
                    }
                }
            }
            store.setAttribute(request,name,value);
        }
        else{
            if(this.temporarySessionAttributes==null){
                this.temporarySessionAttributes=(Map<String,Serializable>)new HashMap(3);
            }
            this.temporarySessionAttributes.put(name,value);
        }
    }
    public synchronized int nextSequenceValue(){
        return this.sequence++;
    }
    public synchronized int nextPageId(){
        return this.pageId++;
    }
    public final IPageManager getPageManager(){
        final IPageManager pageManager=Application.get().internalGetPageManager();
        return ((PageAccessSynchronizer)this.pageAccessSynchronizer.get()).adapt(pageManager);
    }
    public void onEvent(final IEvent<?> event){
    }
    public void onInvalidate(){
    }
    static{
        log=LoggerFactory.getLogger(Session.class);
    }
    private static final class PageAccessSynchronizerProvider extends LazyInitializer<PageAccessSynchronizer>{
        private static final long serialVersionUID=1L;
        protected PageAccessSynchronizer createInstance(){
            Duration timeout;
            if(Application.exists()){
                timeout=Application.get().getRequestCycleSettings().getTimeout();
            }
            else{
                timeout=Duration.minutes(1);
                Session.log.warn("PageAccessSynchronizer created outside of application thread, using default timeout: {}",timeout);
            }
            return new PageAccessSynchronizer(timeout);
        }
    }
}
