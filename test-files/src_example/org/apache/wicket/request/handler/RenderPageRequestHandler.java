package org.apache.wicket.request.handler;

import org.apache.wicket.request.handler.logger.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.handler.render.*;
import org.apache.wicket.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class RenderPageRequestHandler implements IPageRequestHandler,IPageClassRequestHandler,ILoggableRequestHandler{
    private static final Logger logger;
    private final IPageProvider pageProvider;
    private final RedirectPolicy redirectPolicy;
    private PageLogData logData;
    public RenderPageRequestHandler(final IPageProvider pageProvider){
        this(pageProvider,RedirectPolicy.AUTO_REDIRECT);
    }
    public RenderPageRequestHandler(final IPageProvider pageProvider,final RedirectPolicy redirectPolicy){
        super();
        Args.notNull((Object)pageProvider,"pageProvider");
        Args.notNull((Object)redirectPolicy,"redirectPolicy");
        this.redirectPolicy=redirectPolicy;
        this.pageProvider=pageProvider;
    }
    public IPageProvider getPageProvider(){
        return this.pageProvider;
    }
    public RedirectPolicy getRedirectPolicy(){
        return this.redirectPolicy;
    }
    public Class<? extends IRequestablePage> getPageClass(){
        return this.pageProvider.getPageClass();
    }
    public Integer getPageId(){
        return this.pageProvider.getPageId();
    }
    public PageParameters getPageParameters(){
        return this.pageProvider.getPageParameters();
    }
    public void detach(final IRequestCycle requestCycle){
        if(this.logData==null){
            this.logData=new PageLogData(this.pageProvider);
        }
        this.pageProvider.detach();
    }
    public PageLogData getLogData(){
        return this.logData;
    }
    public IRequestablePage getPage(){
        return this.pageProvider.getPageInstance();
    }
    public void respond(final IRequestCycle requestCycle){
        final PageRenderer renderer=(PageRenderer)Application.get().getPageRendererProvider().get((Object)this);
        renderer.respond((RequestCycle)requestCycle);
    }
    public final boolean isPageInstanceCreated(){
        if(!(this.pageProvider instanceof IIntrospectablePageProvider)){
            RenderPageRequestHandler.logger.warn("{} used by this application does not implement {}, the request handler is falling back on using incorrect behavior",IPageProvider.class,IIntrospectablePageProvider.class);
            return !this.pageProvider.isNewPageInstance();
        }
        return ((IIntrospectablePageProvider)this.pageProvider).hasPageInstance();
    }
    public final Integer getRenderCount(){
        return this.pageProvider.getRenderCount();
    }
    static{
        logger=LoggerFactory.getLogger(RenderPageRequestHandler.class);
    }
    public enum RedirectPolicy{
        ALWAYS_REDIRECT,NEVER_REDIRECT,AUTO_REDIRECT;
    }
}
