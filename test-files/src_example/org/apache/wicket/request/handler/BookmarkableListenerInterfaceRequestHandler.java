package org.apache.wicket.request.handler;

import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class BookmarkableListenerInterfaceRequestHandler implements IPageRequestHandler,IComponentRequestHandler{
    private static final Logger logger;
    private final IPageAndComponentProvider pageComponentProvider;
    private final RequestListenerInterface listenerInterface;
    private final Integer behaviorIndex;
    public BookmarkableListenerInterfaceRequestHandler(final IPageAndComponentProvider pageComponentProvider,final RequestListenerInterface listenerInterface,final Integer behaviorIndex){
        super();
        Args.notNull((Object)pageComponentProvider,"pageComponentProvider");
        Args.notNull((Object)listenerInterface,"listenerInterface");
        this.pageComponentProvider=pageComponentProvider;
        this.listenerInterface=listenerInterface;
        this.behaviorIndex=behaviorIndex;
    }
    public BookmarkableListenerInterfaceRequestHandler(final PageAndComponentProvider pageComponentProvider,final RequestListenerInterface listenerInterface){
        this(pageComponentProvider,listenerInterface,null);
    }
    public IRequestableComponent getComponent(){
        return this.pageComponentProvider.getComponent();
    }
    public final String getComponentPath(){
        return this.pageComponentProvider.getComponentPath();
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
        this.pageComponentProvider.detach();
    }
    public RequestListenerInterface getListenerInterface(){
        return this.listenerInterface;
    }
    public Integer getBehaviorIndex(){
        return this.behaviorIndex;
    }
    public void respond(final IRequestCycle requestCycle){
    }
    public final boolean isPageInstanceCreated(){
        return true;
    }
    public final Integer getRenderCount(){
        return this.pageComponentProvider.getRenderCount();
    }
    static{
        logger=LoggerFactory.getLogger(BookmarkableListenerInterfaceRequestHandler.class);
    }
}
