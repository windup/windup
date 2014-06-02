package org.apache.wicket.request.cycle;

import org.apache.wicket.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.*;

public class PageRequestHandlerTracker extends AbstractRequestCycleListener{
    public static final MetaDataKey<IPageRequestHandler> FIRST_HANDLER_KEY;
    public static final MetaDataKey<IPageRequestHandler> LAST_HANDLER_KEY;
    public void onRequestHandlerResolved(final RequestCycle cycle,final IRequestHandler handler){
        super.onRequestHandlerResolved(cycle,handler);
        this.registerFirstHandler(cycle,handler);
        this.registerLastHandler(cycle,handler);
    }
    public void onRequestHandlerScheduled(final RequestCycle cycle,final IRequestHandler handler){
        super.onRequestHandlerResolved(cycle,handler);
        this.registerLastHandler(cycle,handler);
    }
    private void registerLastHandler(final RequestCycle cycle,final IRequestHandler handler){
        if(handler instanceof IPageRequestHandler){
            cycle.setMetaData(PageRequestHandlerTracker.LAST_HANDLER_KEY,(IPageRequestHandler)handler);
        }
    }
    private void registerFirstHandler(final RequestCycle cycle,final IRequestHandler handler){
        if(handler instanceof IPageRequestHandler&&getFirstHandler(cycle)==null){
            cycle.setMetaData(PageRequestHandlerTracker.FIRST_HANDLER_KEY,(IPageRequestHandler)handler);
        }
    }
    public static IPageRequestHandler getLastHandler(final RequestCycle cycle){
        return cycle.getMetaData(PageRequestHandlerTracker.LAST_HANDLER_KEY);
    }
    public static IPageRequestHandler getFirstHandler(final RequestCycle cycle){
        return cycle.getMetaData(PageRequestHandlerTracker.FIRST_HANDLER_KEY);
    }
    static{
        FIRST_HANDLER_KEY=new MetaDataKey<IPageRequestHandler>() {};
        LAST_HANDLER_KEY=new MetaDataKey<IPageRequestHandler>() {};
    }
}
