package org.apache.wicket.request.handler;

import org.apache.wicket.request.component.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.*;

public class PageAndComponentProvider extends PageProvider implements IPageAndComponentProvider{
    private IRequestableComponent component;
    private String componentPath;
    public PageAndComponentProvider(final IRequestablePage page,final String componentPath){
        super(page);
        this.setComponentPath(componentPath);
    }
    public PageAndComponentProvider(final IRequestablePage page,final IRequestableComponent component){
        super(page);
        Args.notNull((Object)component,"component");
        this.component=component;
    }
    public PageAndComponentProvider(final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters,final String componentPath){
        super(pageClass,pageParameters);
        this.setComponentPath(componentPath);
    }
    public PageAndComponentProvider(final Class<? extends IRequestablePage> pageClass,final String componentPath){
        super(pageClass);
        this.setComponentPath(componentPath);
    }
    public PageAndComponentProvider(final Integer pageId,final Class<? extends IRequestablePage> pageClass,final Integer renderCount,final String componentPath){
        super(pageId,pageClass,renderCount);
        this.setComponentPath(componentPath);
    }
    public PageAndComponentProvider(final Integer pageId,final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters,final Integer renderCount,final String componentPath){
        super(pageId,pageClass,pageParameters,renderCount);
        this.setComponentPath(componentPath);
    }
    public PageAndComponentProvider(final Integer pageId,final Integer renderCount,final String componentPath){
        super(pageId,renderCount);
        this.setComponentPath(componentPath);
    }
    public PageAndComponentProvider(final IRequestablePage page,final IRequestableComponent component,final PageParameters parameters){
        super(page);
        Args.notNull((Object)component,"component");
        this.component=component;
        if(parameters!=null){
            this.setPageParameters(parameters);
        }
    }
    public IRequestableComponent getComponent(){
        if(this.component==null){
            final IRequestablePage page=this.getPageInstance();
            this.component=page.get(this.componentPath);
            if(this.component==null&&page.isPageStateless()){
                final Page p=(Page)page;
                p.internalInitialize();
                p.internalPrepareForRender(false);
                this.component=page.get(this.componentPath);
            }
        }
        if(this.component==null){
            throw new ComponentNotFoundException("Could not find component '"+this.componentPath+"' on page '"+this.getPageClass());
        }
        return this.component;
    }
    public String getComponentPath(){
        if(this.componentPath!=null){
            return this.componentPath;
        }
        return this.component.getPageRelativePath();
    }
    private void setComponentPath(final String componentPath){
        Args.notNull((Object)componentPath,"componentPath");
        this.componentPath=componentPath;
    }
}
