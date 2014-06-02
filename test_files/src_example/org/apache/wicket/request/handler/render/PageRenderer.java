package org.apache.wicket.request.handler.render;

import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.*;
import org.apache.wicket.request.cycle.*;

public abstract class PageRenderer{
    private final RenderPageRequestHandler renderPageRequestHandler;
    public PageRenderer(final RenderPageRequestHandler renderPageRequestHandler){
        super();
        this.renderPageRequestHandler=renderPageRequestHandler;
    }
    protected IPageProvider getPageProvider(){
        return this.renderPageRequestHandler.getPageProvider();
    }
    protected RenderPageRequestHandler.RedirectPolicy getRedirectPolicy(){
        return this.renderPageRequestHandler.getRedirectPolicy();
    }
    protected RenderPageRequestHandler getRenderPageRequestHandler(){
        return this.renderPageRequestHandler;
    }
    protected IRequestablePage getPage(){
        return this.getPageProvider().getPageInstance();
    }
    protected boolean isOnePassRender(){
        return Application.get().getRequestCycleSettings().getRenderStrategy()==IRequestCycleSettings.RenderStrategy.ONE_PASS_RENDER;
    }
    protected boolean isRedirectToRender(){
        return Application.get().getRequestCycleSettings().getRenderStrategy()==IRequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER;
    }
    protected boolean isRedirectToBuffer(){
        return Application.get().getRequestCycleSettings().getRenderStrategy()==IRequestCycleSettings.RenderStrategy.REDIRECT_TO_BUFFER;
    }
    protected String getSessionId(){
        return Session.get().getId();
    }
    protected boolean isSessionTemporary(){
        return Session.get().isTemporary();
    }
    protected boolean enableRedirectForStatelessPage(){
        return true;
    }
    public abstract void respond(final RequestCycle p0);
}
