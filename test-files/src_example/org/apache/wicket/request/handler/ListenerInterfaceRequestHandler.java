package org.apache.wicket.request.handler;

import org.apache.wicket.request.handler.logger.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class ListenerInterfaceRequestHandler implements IPageRequestHandler,IComponentRequestHandler,ILoggableRequestHandler{
    private static final Logger LOG;
    private final IPageAndComponentProvider pageComponentProvider;
    private final RequestListenerInterface listenerInterface;
    private final Integer behaviorId;
    private ListenerInterfaceLogData logData;
    public ListenerInterfaceRequestHandler(final IPageAndComponentProvider pageComponentProvider,final RequestListenerInterface listenerInterface,final Integer behaviorIndex){
        super();
        Args.notNull((Object)pageComponentProvider,"pageComponentProvider");
        Args.notNull((Object)listenerInterface,"listenerInterface");
        this.pageComponentProvider=pageComponentProvider;
        this.listenerInterface=listenerInterface;
        this.behaviorId=behaviorIndex;
    }
    public ListenerInterfaceRequestHandler(final PageAndComponentProvider pageComponentProvider,final RequestListenerInterface listenerInterface){
        this(pageComponentProvider,listenerInterface,null);
    }
    public IRequestableComponent getComponent(){
        return this.pageComponentProvider.getComponent();
    }
    public IRequestablePage getPage(){
        return this.pageComponentProvider.getPageInstance();
    }
    public Class<? extends IRequestablePage> getPageClass(){
        return this.pageComponentProvider.getPageClass();
    }
    public Integer getPageId(){
        return this.pageComponentProvider.getPageId();
    }
    public PageParameters getPageParameters(){
        return this.pageComponentProvider.getPageParameters();
    }
    public void detach(final IRequestCycle requestCycle){
        if(this.logData==null){
            this.logData=new ListenerInterfaceLogData(this.pageComponentProvider,this.listenerInterface,this.behaviorId);
        }
        this.pageComponentProvider.detach();
    }
    public RequestListenerInterface getListenerInterface(){
        return this.listenerInterface;
    }
    public Integer getBehaviorIndex(){
        return this.behaviorId;
    }
    public void respond(final IRequestCycle requestCycle){
        final IRequestablePage page=this.getPage();
        final boolean freshPage=this.pageComponentProvider.isPageInstanceFresh();
        final boolean isAjax=((WebRequest)requestCycle.getRequest()).isAjax();
        IRequestableComponent component=null;
        try{
            component=this.getComponent();
        }
        catch(ComponentNotFoundException e){
            component=null;
        }
        if((component!=null||!freshPage)&&(component==null||this.getComponent().getPage()!=page)){
            throw new WicketRuntimeException("Component "+this.getComponent()+" has been removed from page.");
        }
        if(page instanceof Page){
            ((Page)page).internalInitialize();
        }
        final boolean isStateless=page.isPageStateless();
        RenderPageRequestHandler.RedirectPolicy policy=isStateless?RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT:RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT;
        final IPageProvider pageProvider=new PageProvider(page);
        if(freshPage&&(!isStateless||component==null)){
            if(ListenerInterfaceRequestHandler.LOG.isDebugEnabled()){
                ListenerInterfaceRequestHandler.LOG.debug("A ListenerInterface '{}' assigned to '{}' is executed on an expired stateful page. Scheduling re-create of the page and ignoring the listener interface...",this.listenerInterface,this.getComponentPath());
            }
            if(isAjax){
                policy=RenderPageRequestHandler.RedirectPolicy.ALWAYS_REDIRECT;
            }
            requestCycle.scheduleRequestHandlerAfterCurrent((IRequestHandler)new RenderPageRequestHandler(pageProvider,policy));
            return;
        }
        if(!isAjax&&this.listenerInterface.isRenderPageAfterInvocation()){
            requestCycle.scheduleRequestHandlerAfterCurrent((IRequestHandler)new RenderPageRequestHandler(pageProvider,policy));
        }
        this.invokeListener();
    }
    private void invokeListener(){
        if(this.getBehaviorIndex()==null){
            this.listenerInterface.invoke(this.getComponent());
        }
        else{
            try{
                final Behavior behavior=this.getComponent().getBehaviorById(this.behaviorId);
                this.listenerInterface.invoke(this.getComponent(),behavior);
            }
            catch(IndexOutOfBoundsException e){
                throw new WicketRuntimeException("Couldn't find component behavior.",e);
            }
        }
    }
    public final boolean isPageInstanceCreated(){
        if(!(this.pageComponentProvider instanceof IIntrospectablePageProvider)){
            ListenerInterfaceRequestHandler.LOG.warn("{} used by this application does not implement {}, the request handler is falling back on using incorrect behavior",IPageProvider.class,IIntrospectablePageProvider.class);
            return !this.pageComponentProvider.isNewPageInstance();
        }
        return this.pageComponentProvider.hasPageInstance();
    }
    public final String getComponentPath(){
        return this.pageComponentProvider.getComponentPath();
    }
    public final Integer getRenderCount(){
        return this.pageComponentProvider.getRenderCount();
    }
    public ListenerInterfaceLogData getLogData(){
        return this.logData;
    }
    static{
        LOG=LoggerFactory.getLogger(ListenerInterfaceRequestHandler.class);
    }
}
