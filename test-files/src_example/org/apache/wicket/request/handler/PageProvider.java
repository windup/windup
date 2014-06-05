package org.apache.wicket.request.handler;

import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.*;
import org.apache.wicket.request.mapper.*;

public class PageProvider implements IPageProvider,IIntrospectablePageProvider{
    private final Integer renderCount;
    private final Integer pageId;
    private IPageSource pageSource;
    private IRequestablePage pageInstance;
    private boolean pageInstanceIsFresh;
    private Class<? extends IRequestablePage> pageClass;
    private PageParameters pageParameters;
    public PageProvider(final Integer pageId,final Integer renderCount){
        super();
        this.pageId=pageId;
        this.renderCount=renderCount;
    }
    public PageProvider(final Integer pageId,final Class<? extends IRequestablePage> pageClass,final Integer renderCount){
        this(pageId,pageClass,new PageParameters(),renderCount);
    }
    public PageProvider(final Integer pageId,final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters,final Integer renderCount){
        super();
        this.pageId=pageId;
        this.setPageClass(pageClass);
        this.setPageParameters(pageParameters);
        this.renderCount=renderCount;
    }
    public PageProvider(final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters){
        super();
        this.setPageClass(pageClass);
        if(pageParameters!=null){
            this.setPageParameters(pageParameters);
        }
        this.pageId=null;
        this.renderCount=null;
    }
    public PageProvider(final Class<? extends IRequestablePage> pageClass){
        this(pageClass,null);
    }
    public PageProvider(final IRequestablePage page){
        super();
        Args.notNull((Object)page,"page");
        this.pageInstance=page;
        this.pageId=page.getPageId();
        this.renderCount=page.getRenderCount();
    }
    public IRequestablePage getPageInstance(){
        if(this.pageInstance==null){
            this.resolvePageInstance(this.pageId,this.pageClass,this.pageParameters,this.renderCount);
            if(this.pageInstance==null){
                throw new PageExpiredException("Page with id '"+this.pageId+"' has expired.");
            }
        }
        return this.pageInstance;
    }
    public PageParameters getPageParameters(){
        if(this.pageParameters!=null){
            return this.pageParameters;
        }
        if(!this.isNewPageInstance()){
            return this.pageInstance.getPageParameters();
        }
        return null;
    }
    public boolean isNewPageInstance(){
        boolean isNew=this.pageInstance==null;
        if(isNew&&this.pageId!=null){
            final IRequestablePage storedPageInstance=this.getStoredPage(this.pageId);
            if(storedPageInstance!=null){
                this.pageInstance=storedPageInstance;
                isNew=false;
            }
        }
        return isNew;
    }
    public Class<? extends IRequestablePage> getPageClass(){
        if(this.pageClass!=null){
            return this.pageClass;
        }
        return (Class<? extends IRequestablePage>)this.getPageInstance().getClass();
    }
    protected IPageSource getPageSource(){
        if(this.pageSource!=null){
            return this.pageSource;
        }
        if(Application.exists()){
            return Application.get().getMapperContext();
        }
        throw new IllegalStateException("No application is bound to current thread. Call setPageSource() to manually assign pageSource to this provider.");
    }
    private void resolvePageInstance(final Integer pageId,final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters,final Integer renderCount){
        IRequestablePage page=null;
        boolean freshCreated=false;
        if(pageId!=null){
            page=this.getStoredPage(pageId);
        }
        if(page==null&&pageClass!=null){
            PageParameters parameters;
            if(pageId!=null){
                parameters=new PageParameters();
            }
            else{
                parameters=pageParameters;
            }
            page=this.getPageSource().newPageInstance(pageClass,parameters);
            freshCreated=true;
        }
        if(page!=null&&!freshCreated&&renderCount!=null&&page.getRenderCount()!=renderCount){
            throw new StalePageException(page);
        }
        this.pageInstanceIsFresh=freshCreated;
        this.pageInstance=page;
    }
    private IRequestablePage getStoredPage(final int pageId){
        IRequestablePage storedPageInstance=this.getPageSource().getPageInstance(pageId);
        if(storedPageInstance!=null){
            if(this.pageClass==null||this.pageClass.equals(storedPageInstance.getClass())){
                this.pageInstance=storedPageInstance;
                this.pageInstanceIsFresh=false;
                if(this.renderCount!=null&&this.pageInstance.getRenderCount()!=this.renderCount){
                    throw new StalePageException(this.pageInstance);
                }
            }
            else{
                storedPageInstance=null;
            }
        }
        return storedPageInstance;
    }
    public void detach(){
        if(this.pageInstance!=null){
            this.pageInstance.detach();
        }
    }
    public void setPageSource(final IPageSource pageSource){
        this.pageSource=pageSource;
    }
    private void setPageClass(final Class<? extends IRequestablePage> pageClass){
        Args.notNull((Object)pageClass,"pageClass");
        this.pageClass=pageClass;
    }
    protected void setPageParameters(final PageParameters pageParameters){
        this.pageParameters=pageParameters;
    }
    public Integer getPageId(){
        return this.pageId;
    }
    public Integer getRenderCount(){
        return this.renderCount;
    }
    public final boolean hasPageInstance(){
        if(this.pageInstance==null&&this.pageId!=null){
            this.getStoredPage(this.pageId);
        }
        return this.pageInstance!=null;
    }
    public final boolean isPageInstanceFresh(){
        if(!this.hasPageInstance()){
            throw new IllegalStateException("Page instance not yet resolved");
        }
        return this.pageInstanceIsFresh;
    }
}
