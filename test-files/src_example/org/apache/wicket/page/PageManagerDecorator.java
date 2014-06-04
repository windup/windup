package org.apache.wicket.page;

import org.apache.wicket.util.lang.*;

public class PageManagerDecorator implements IPageManager{
    private final IPageManager delegate;
    public PageManagerDecorator(final IPageManager delegate){
        super();
        Args.notNull((Object)delegate,"delegate");
        this.delegate=delegate;
    }
    public IPageManagerContext getContext(){
        return this.delegate.getContext();
    }
    public IManageablePage getPage(final int id){
        return this.delegate.getPage(id);
    }
    public void touchPage(final IManageablePage page){
        this.delegate.touchPage(page);
    }
    public boolean supportsVersioning(){
        return this.delegate.supportsVersioning();
    }
    public void commitRequest(){
        this.delegate.commitRequest();
    }
    public void newSessionCreated(){
        this.delegate.newSessionCreated();
    }
    public void sessionExpired(final String sessionId){
        this.delegate.sessionExpired(sessionId);
    }
    public void destroy(){
        this.delegate.destroy();
    }
}
