package org.apache.wicket.request.cycle;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.event.*;
import org.slf4j.*;

public class RequestCycle implements IRequestCycle,IEventSink{
    private static final Logger log;
    private boolean cleanupFeedbackMessagesOnDetach;
    private Request request;
    private final Response originalResponse;
    private final IRequestMapper requestMapper;
    private final IExceptionMapper exceptionMapper;
    private final RequestCycleListenerCollection listeners;
    private UrlRenderer urlRenderer;
    private MetaDataEntry<?>[] metaData;
    private final long startTime;
    private final RequestHandlerStack requestHandlerExecutor;
    private Response activeResponse;
    public static RequestCycle get(){
        return ThreadContext.getRequestCycle();
    }
    private static void set(final RequestCycle requestCycle){
        ThreadContext.setRequestCycle(requestCycle);
    }
    public RequestCycle(final RequestCycleContext context){
        super();
        Args.notNull((Object)context,"context");
        Args.notNull((Object)context.getRequest(),"context.request");
        Args.notNull((Object)context.getResponse(),"context.response");
        Args.notNull((Object)context.getRequestMapper(),"context.requestMapper");
        Args.notNull((Object)context.getExceptionMapper(),"context.exceptionMapper");
        this.cleanupFeedbackMessagesOnDetach=true;
        this.listeners=new RequestCycleListenerCollection();
        this.startTime=System.currentTimeMillis();
        this.requestHandlerExecutor=new HandlerExecutor();
        this.activeResponse=context.getResponse();
        this.request=context.getRequest();
        this.originalResponse=context.getResponse();
        this.requestMapper=context.getRequestMapper();
        this.exceptionMapper=context.getExceptionMapper();
    }
    protected UrlRenderer newUrlRenderer(){
        return new UrlRenderer(this.getRequest());
    }
    public Response getOriginalResponse(){
        return this.originalResponse;
    }
    public final UrlRenderer getUrlRenderer(){
        if(this.urlRenderer==null){
            this.urlRenderer=this.newUrlRenderer();
        }
        return this.urlRenderer;
    }
    protected IRequestHandler resolveRequestHandler(){
        return this.requestMapper.mapRequest(this.request);
    }
    protected int getExceptionRetryCount(){
        return 10;
    }
    public boolean processRequest(){
        try{
            set(this);
            this.listeners.onBeginRequest(this);
            this.onBeginRequest();
            final IRequestHandler handler=this.resolveRequestHandler();
            if(handler!=null){
                this.execute(handler);
                return true;
            }
            RequestCycle.log.debug("No suitable handler found for URL {}, falling back to container to process this request",this.request.getUrl());
        }
        catch(Exception e){
            final IRequestHandler handler2=this.handleException(e);
            if(handler2!=null){
                this.listeners.onExceptionRequestHandlerResolved(this,handler2,e);
                this.executeExceptionRequestHandler(handler2,this.getExceptionRetryCount());
                this.listeners.onRequestHandlerExecuted(this,handler2);
            }
            else{
                RequestCycle.log.error("Error during request processing. URL="+this.request.getUrl(),e);
            }
            return true;
        }
        finally{
            set(null);
        }
        return false;
    }
    private void execute(final IRequestHandler handler){
        Args.notNull((Object)handler,"handler");
        try{
            this.listeners.onRequestHandlerResolved(this,handler);
            this.requestHandlerExecutor.execute(handler);
            this.listeners.onRequestHandlerExecuted(this,handler);
        }
        catch(RuntimeException e){
            final IRequestHandler replacement=this.requestHandlerExecutor.resolveHandler(e);
            if(replacement==null){
                throw e;
            }
            this.execute(replacement);
        }
    }
    public boolean processRequestAndDetach(){
        boolean result;
        try{
            result=this.processRequest();
        }
        finally{
            this.detach();
        }
        return result;
    }
    private void executeExceptionRequestHandler(final IRequestHandler handler,final int retryCount){
        this.scheduleRequestHandlerAfterCurrent(null);
        try{
            this.requestHandlerExecutor.execute(handler);
        }
        catch(Exception e){
            if(retryCount>0){
                final IRequestHandler next=this.handleException(e);
                if(next!=null){
                    this.executeExceptionRequestHandler(next,retryCount-1);
                    return;
                }
            }
            RequestCycle.log.error("Error during processing error message",e);
        }
    }
    protected IRequestHandler handleException(final Exception e){
        final IRequestHandler handler=this.listeners.onException(this,e);
        if(handler!=null){
            return handler;
        }
        return this.exceptionMapper.map(e);
    }
    public Request getRequest(){
        return this.request;
    }
    public void setRequest(final Request request){
        this.request=request;
    }
    public final <T> void setMetaData(final MetaDataKey<T> key,final T object){
        this.metaData=key.set(this.metaData,object);
    }
    public final <T> T getMetaData(final MetaDataKey<T> key){
        return key.get(this.metaData);
    }
    public Url mapUrlFor(final IRequestHandler handler){
        final Url url=this.requestMapper.mapHandler(handler);
        this.listeners.onUrlMapped(this,handler,url);
        return url;
    }
    public Url mapUrlFor(final ResourceReference reference,final PageParameters params){
        return this.mapUrlFor((IRequestHandler)new ResourceReferenceRequestHandler(reference,params));
    }
    public final <C extends Page> Url mapUrlFor(final Class<C> pageClass,final PageParameters parameters){
        final IRequestHandler handler=(IRequestHandler)new BookmarkablePageRequestHandler(new PageProvider(pageClass,parameters));
        return this.mapUrlFor(handler);
    }
    public final CharSequence urlFor(final ResourceReference reference,final PageParameters params){
        final ResourceReferenceRequestHandler handler=new ResourceReferenceRequestHandler(reference,params);
        return this.urlFor((IRequestHandler)handler);
    }
    public final <C extends Page> CharSequence urlFor(final Class<C> pageClass,final PageParameters parameters){
        final IRequestHandler handler=(IRequestHandler)new BookmarkablePageRequestHandler(new PageProvider(pageClass,parameters));
        return this.urlFor(handler);
    }
    public CharSequence urlFor(final IRequestHandler handler){
        final Url mappedUrl=this.mapUrlFor(handler);
        final CharSequence url=(CharSequence)this.renderUrl(mappedUrl,handler);
        return url;
    }
    private String renderUrl(final Url url,final IRequestHandler handler){
        if(url!=null){
            final boolean shouldEncodeStaticResource=Application.exists()&&Application.get().getResourceSettings().isEncodeJSessionId();
            String renderedUrl=this.getUrlRenderer().renderUrl(url);
            if(handler instanceof ResourceReferenceRequestHandler){
                final ResourceReferenceRequestHandler rrrh=(ResourceReferenceRequestHandler)handler;
                final IResource resource=rrrh.getResource();
                if(!(resource instanceof IStaticCacheableResource)||shouldEncodeStaticResource){
                    renderedUrl=this.getOriginalResponse().encodeURL((CharSequence)renderedUrl);
                }
            }
            else if(handler instanceof ResourceRequestHandler){
                final ResourceRequestHandler rrh=(ResourceRequestHandler)handler;
                final IResource resource=rrh.getResource();
                if(!(resource instanceof IStaticCacheableResource)||shouldEncodeStaticResource){
                    renderedUrl=this.getOriginalResponse().encodeURL((CharSequence)renderedUrl);
                }
            }
            else{
                renderedUrl=this.getOriginalResponse().encodeURL((CharSequence)renderedUrl);
            }
            return renderedUrl;
        }
        return null;
    }
    public final void detach(){
        set(this);
        try{
            this.onDetach();
        }
        finally{
            try{
                this.onInternalDetach();
            }
            finally{
                set(null);
            }
        }
    }
    private void onInternalDetach(){
        if(Session.exists()){
            Session.get().internalDetach();
        }
        if(Application.exists()){
            final IRequestLogger requestLogger=Application.get().getRequestLogger();
            if(requestLogger instanceof IStagedRequestLogger){
                ((IStagedRequestLogger)requestLogger).performLogging();
            }
        }
    }
    public void onDetach(){
        if(this.cleanupFeedbackMessagesOnDetach&&Session.exists()){
            Session.get().cleanupFeedbackMessages();
        }
        try{
            this.onEndRequest();
            this.listeners.onEndRequest(this);
        }
        catch(RuntimeException e){
            RequestCycle.log.error("Exception occurred during onEndRequest",e);
        }
        try{
            this.requestHandlerExecutor.detach();
        }
        finally{
            this.listeners.onDetach(this);
        }
        if(Session.exists()){
            Session.get().detach();
        }
    }
    public void setResponsePage(final IRequestablePage page){
        if(page instanceof Page){
            ((Page)page).setStatelessHint(false);
        }
        this.scheduleRequestHandlerAfterCurrent((IRequestHandler)new RenderPageRequestHandler(new PageProvider(page),RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
    }
    public void setResponsePage(final Class<? extends IRequestablePage> pageClass){
        final IPageProvider provider=new PageProvider(pageClass,null);
        this.scheduleRequestHandlerAfterCurrent((IRequestHandler)new RenderPageRequestHandler(provider,RenderPageRequestHandler.RedirectPolicy.ALWAYS_REDIRECT));
    }
    public void setResponsePage(final Class<? extends IRequestablePage> pageClass,final PageParameters parameters){
        final IPageProvider provider=new PageProvider(pageClass,parameters);
        this.scheduleRequestHandlerAfterCurrent((IRequestHandler)new RenderPageRequestHandler(provider,RenderPageRequestHandler.RedirectPolicy.ALWAYS_REDIRECT));
    }
    public boolean isCleanupFeedbackMessagesOnDetach(){
        return this.cleanupFeedbackMessagesOnDetach;
    }
    public void setCleanupFeedbackMessagesOnDetach(final boolean cleanupFeedbackMessagesOnDetach){
        this.cleanupFeedbackMessagesOnDetach=cleanupFeedbackMessagesOnDetach;
    }
    public final long getStartTime(){
        return this.startTime;
    }
    public void onEvent(final IEvent<?> event){
    }
    protected void onBeginRequest(){
    }
    protected void onEndRequest(){
    }
    public RequestCycleListenerCollection getListeners(){
        return this.listeners;
    }
    public Response getResponse(){
        return this.activeResponse;
    }
    public Response setResponse(final Response response){
        final Response current=this.activeResponse;
        this.activeResponse=response;
        return current;
    }
    public void scheduleRequestHandlerAfterCurrent(final IRequestHandler handler){
        this.requestHandlerExecutor.schedule(handler);
        if(handler!=null){
            this.listeners.onRequestHandlerScheduled(this,handler);
        }
    }
    public IRequestHandler getActiveRequestHandler(){
        return this.requestHandlerExecutor.getActive();
    }
    public IRequestHandler getRequestHandlerScheduledAfterCurrent(){
        return this.requestHandlerExecutor.next();
    }
    public void replaceAllRequestHandlers(final IRequestHandler handler){
        this.requestHandlerExecutor.replaceAll(handler);
    }
    static{
        log=LoggerFactory.getLogger(RequestCycle.class);
    }
    private class HandlerExecutor extends RequestHandlerStack{
        protected void respond(final IRequestHandler handler){
            final Response originalResponse=RequestCycle.this.getResponse();
            try{
                handler.respond((IRequestCycle)RequestCycle.this);
            }
            finally{
                RequestCycle.this.setResponse(originalResponse);
            }
        }
        protected void detach(final IRequestHandler handler){
            handler.detach((IRequestCycle)RequestCycle.this);
        }
    }
}
