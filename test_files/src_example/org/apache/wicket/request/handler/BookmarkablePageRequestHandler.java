package org.apache.wicket.request.handler;

import org.apache.wicket.request.handler.logger.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;

public class BookmarkablePageRequestHandler implements IPageClassRequestHandler,ILoggableRequestHandler{
    private final IPageProvider pageProvider;
    private PageLogData logData;
    public BookmarkablePageRequestHandler(final IPageProvider pageProvider){
        super();
        Args.notNull((Object)pageProvider,"pageProvider");
        this.pageProvider=pageProvider;
    }
    public Class<? extends IRequestablePage> getPageClass(){
        return this.pageProvider.getPageClass();
    }
    public PageParameters getPageParameters(){
        return this.pageProvider.getPageParameters();
    }
    public void respond(final IRequestCycle requestCycle){
    }
    public void detach(final IRequestCycle requestCycle){
        if(this.logData==null){
            this.logData=new PageLogData(this.pageProvider);
        }
    }
    public PageLogData getLogData(){
        return this.logData;
    }
}
